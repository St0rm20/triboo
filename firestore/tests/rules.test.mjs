// Pruebas de firestore.rules contra el emulador: npm test (desde firestore/).
// Cada caso reproduce una escritura real de la app (los DTO de data/remote) con el usuario que la hace.

import { readFileSync } from 'node:fs';
import { after, before, beforeEach, describe, test } from 'node:test';
import { assertFails, assertSucceeds, initializeTestEnvironment } from '@firebase/rules-unit-testing';
import {
  deleteDoc, doc, GeoPoint, getDoc, getDocs, collection, setDoc, setLogLevel, Timestamp, updateDoc, writeBatch
} from 'firebase/firestore';

// Los PERMISSION_DENIED esperados ensucian la salida
setLogLevel('silent');

let env;

before(async () => {
  env = await initializeTestEnvironment({
    projectId: 'demo-tribooo',
    firestore: { rules: readFileSync(new URL('../firestore.rules', import.meta.url), 'utf8') }
  });
});
after(() => env.cleanup());
beforeEach(() => env.clearFirestore());

// ---------- Datos como los escriben los DTO ----------

const now = () => Timestamp.now();
const later = (hours) => Timestamp.fromMillis(Date.now() + hours * 3_600_000);

const user = (id, role = 'USER') => ({
  id, name: `Usuario ${id}`, email: `${id}@triboo.co`, city: 'Armenia', neighborhood: 'Granada',
  bio: null, photo: null, role, dateRegistered: now()
});

const event = (id, organizerId, overrides = {}) => ({
  id, title: 'Ciclopaseo', description: 'Recorrido por el norte', startDate: later(24), endDate: later(26),
  multidia: false, place: 'Parque de la Vida', neighborhood: 'Norte', location: new GeoPoint(4.5446, -75.6658),
  cupo: 1, contribution: { amount: 5000.5, cause: null }, status: 'PENDIENTE', verified: false,
  publicationDate: now(), organizerId, categoryId: 'deportes', imageUrl: null, ...overrides
});

const comment = (id, eventId, authorId, overrides = {}) => ({
  id, eventId, authorId, text: 'Muy bueno', date: now(), rating: 5, parentCommentId: null, ...overrides
});

const notification = (id, destinationUserId, type, reference) => ({
  id, destinationUserId, type, reference, read: false, date: now()
});

/** Escribe documentos sin pasar por las reglas (estado inicial de cada prueba). */
async function given(docs) {
  await env.withSecurityRulesDisabled(async (ctx) => {
    for (const [path, data] of Object.entries(docs)) await setDoc(doc(ctx.firestore(), path), data);
  });
}

const as = (uid) => env.authenticatedContext(uid).firestore();
const anonymous = () => env.unauthenticatedContext().firestore();

// Escenario base: organizadora, vecino, administradora y un evento activo con cupo 1
const base = () => given({
  'users/org': user('org'),
  'users/bob': user('bob'),
  'users/admin': user('admin', 'ADMIN'),
  'events/e1': event('e1', 'org', { status: 'ACTIVO', verified: true })
});

// ---------- Pruebas ----------

describe('catálogos', () => {
  test('se leen sin sesión y nadie los escribe desde la app', async () => {
    await given({ 'cities/armenia': { id: 'armenia', name: 'Armenia' } });
    await assertSucceeds(getDocs(collection(anonymous(), 'cities')));
    await assertFails(setDoc(doc(as('admin'), 'cities/pereira'), { id: 'pereira', name: 'Pereira' }));
  });
});

describe('users', () => {
  test('sin sesión no se lee la colección', async () => {
    await assertFails(getDocs(collection(anonymous(), 'users')));
  });

  test('se registra con su uid y rol USER', async () => {
    await assertSucceeds(setDoc(doc(as('ana'), 'users/ana'), user('ana')));
  });

  test('no puede registrarse como ADMIN ni con otro uid', async () => {
    await assertFails(setDoc(doc(as('ana'), 'users/ana'), user('ana', 'ADMIN')));
    await assertFails(setDoc(doc(as('ana'), 'users/otro'), user('otro')));
  });

  test('rechaza campos que no están en el schema', async () => {
    await assertFails(setDoc(doc(as('ana'), 'users/ana'), { ...user('ana'), password: '123' }));
  });

  test('edita su perfil pero no su rol', async () => {
    await base();
    await assertSucceeds(updateDoc(doc(as('bob'), 'users/bob'), { bio: 'Hola' }));
    await assertFails(updateDoc(doc(as('bob'), 'users/bob'), { role: 'ADMIN' }));
    await assertFails(updateDoc(doc(as('bob'), 'users/org'), { bio: 'Hackeado' }));
  });
});

describe('events', () => {
  test('se crea pendiente junto con su galería en un lote', async () => {
    await base();
    const db = as('org');
    const batch = writeBatch(db);
    batch.set(doc(db, 'events/e2'), event('e2', 'org'));
    batch.set(doc(db, 'eventImages/i1'), { id: 'i1', eventId: 'e2', url: 'https://x/1.jpg', order: 0 });
    await assertSucceeds(batch.commit());
  });

  test('no se crea activo, verificado ni a nombre de otro', async () => {
    await base();
    await assertFails(setDoc(doc(as('org'), 'events/e2'), event('e2', 'org', { status: 'ACTIVO' })));
    await assertFails(setDoc(doc(as('org'), 'events/e2'), event('e2', 'org', { verified: true })));
    await assertFails(setDoc(doc(as('bob'), 'events/e2'), event('e2', 'org')));
  });

  test('no se crea con fin antes del inicio', async () => {
    await base();
    await assertFails(setDoc(doc(as('org'), 'events/e2'), event('e2', 'org', { endDate: now(), startDate: later(5) })));
  });

  test('el organizador finaliza un evento activo', async () => {
    await base();
    await assertSucceeds(updateDoc(doc(as('org'), 'events/e1'), { status: 'FINALIZADO', endDate: later(25) }));
  });

  test('el organizador no se aprueba a sí mismo', async () => {
    await base();
    await given({ 'events/e2': event('e2', 'org') });
    await assertFails(updateDoc(doc(as('org'), 'events/e2'), { status: 'ACTIVO', verified: true }));
  });

  test('solo el administrador verifica y rechaza', async () => {
    await base();
    await given({ 'events/e2': event('e2', 'org') });
    await assertFails(updateDoc(doc(as('bob'), 'events/e2'), { status: 'ACTIVO', verified: true }));
    await assertSucceeds(updateDoc(doc(as('admin'), 'events/e2'), { status: 'ACTIVO', verified: true }));
    await assertSucceeds(updateDoc(doc(as('admin'), 'events/e2'), { status: 'RECHAZADO', verified: false }));
    await assertFails(updateDoc(doc(as('admin'), 'events/e2'), { status: 'ACTIVO', verified: true, title: 'Otro' }));
  });

  test('el organizador reenvía un evento rechazado', async () => {
    await base();
    await given({ 'events/e2': event('e2', 'org', { status: 'RECHAZADO' }) });
    await assertSucceeds(updateDoc(doc(as('org'), 'events/e2'), { status: 'PENDIENTE', title: 'Corregido' }));
  });

  test('solo el organizador o el admin lo borran', async () => {
    await base();
    await assertFails(deleteDoc(doc(as('bob'), 'events/e1')));
    await assertSucceeds(deleteDoc(doc(as('org'), 'events/e1')));
  });
});

describe('relaciones usuario-evento', () => {
  const attendance = (userId) => ({ userId, eventId: 'e1', confirmationDate: now() });

  test('confirma su propia asistencia, no la de otro', async () => {
    await base();
    await assertSucceeds(setDoc(doc(as('bob'), 'attendances/bob_e1'), attendance('bob')));
    await assertFails(setDoc(doc(as('bob'), 'attendances/org_e1'), attendance('org')));
  });

  test('el id debe ser {userId}_{eventId}', async () => {
    await base();
    await assertFails(setDoc(doc(as('bob'), 'attendances/cualquiera'), attendance('bob')));
  });

  test('al liberar un cupo, quien cancela promueve al primero de la lista de espera', async () => {
    await base();
    await given({
      'attendances/org_e1': attendance('org'),
      'waitlist/bob_e1': { userId: 'bob', eventId: 'e1', joinedAt: now() }
    });
    const db = as('org');
    await assertSucceeds(deleteDoc(doc(db, 'attendances/org_e1')));
    await assertSucceeds(setDoc(doc(db, 'attendances/bob_e1'), attendance('bob')));
    await assertSucceeds(deleteDoc(doc(db, 'waitlist/bob_e1')));
  });

  test('el organizador borra relaciones de otros con sus eventos (borrado de cuenta)', async () => {
    await base();
    await given({ 'saved/bob_e1': { userId: 'bob', eventId: 'e1' } });
    await assertFails(deleteDoc(doc(as('eve'), 'saved/bob_e1')));
    await assertSucceeds(deleteDoc(doc(as('org'), 'saved/bob_e1')));
  });

  test('sigue a otro usuario pero no a sí mismo', async () => {
    await base();
    await assertSucceeds(setDoc(doc(as('bob'), 'follows/bob_org'), { followerId: 'bob', followedId: 'org' }));
    await assertFails(setDoc(doc(as('bob'), 'follows/bob_bob'), { followerId: 'bob', followedId: 'bob' }));
  });
});

describe('comments', () => {
  test('comenta y avisa al organizador', async () => {
    await base();
    const db = as('bob');
    await assertSucceeds(setDoc(doc(db, 'comments/c1'), comment('c1', 'e1', 'bob')));
    await assertSucceeds(setDoc(doc(db, 'notifications/n1'), notification('n1', 'org', 'COMMENT', 'c1')));
  });

  test('no avisa con un comentario ajeno o inexistente', async () => {
    await base();
    await given({ 'comments/c1': comment('c1', 'e1', 'org') });
    await assertFails(setDoc(doc(as('bob'), 'notifications/n1'), notification('n1', 'org', 'COMMENT', 'c1')));
    await assertFails(setDoc(doc(as('bob'), 'notifications/n2'), notification('n2', 'org', 'COMMENT', 'nada')));
  });

  test('solo el organizador responde una reseña, sin calificación', async () => {
    await base();
    await given({ 'comments/c1': comment('c1', 'e1', 'bob') });
    const reply = (author) => comment('r1', 'e1', author, { rating: null, parentCommentId: 'c1' });
    await assertFails(setDoc(doc(as('bob'), 'comments/r1'), reply('bob')));
    await assertFails(setDoc(doc(as('org'), 'comments/r1'), { ...reply('org'), rating: 4 }));
    await assertSucceeds(setDoc(doc(as('org'), 'comments/r1'), reply('org')));
    await assertSucceeds(updateDoc(doc(as('org'), 'comments/r1'), { text: 'Gracias' }));
  });

  test('rating fuera de 1-5 se rechaza', async () => {
    await base();
    await assertFails(setDoc(doc(as('bob'), 'comments/c1'), comment('c1', 'e1', 'bob', { rating: 6 })));
  });
});

describe('notifications', () => {
  test('el destinatario solo la marca como leída', async () => {
    await base();
    await given({ 'notifications/n1': notification('n1', 'bob', 'FOLLOWER', 'org') });
    await assertFails(updateDoc(doc(as('org'), 'notifications/n1'), { read: true }));
    await assertFails(updateDoc(doc(as('bob'), 'notifications/n1'), { type: 'APPROVAL' }));
    await assertSucceeds(updateDoc(doc(as('bob'), 'notifications/n1'), { read: true }));
  });

  test('los avisos de moderación solo los crea un administrador', async () => {
    await base();
    await assertFails(setDoc(doc(as('bob'), 'notifications/n1'), notification('n1', 'org', 'APPROVAL', 'e1')));
    await assertSucceeds(setDoc(doc(as('admin'), 'notifications/n1'), notification('n1', 'org', 'APPROVAL', 'e1')));
  });

  test('FOLLOWER debe referenciar a quien sigue', async () => {
    await base();
    await assertSucceeds(setDoc(doc(as('bob'), 'notifications/n1'), notification('n1', 'org', 'FOLLOWER', 'bob')));
    await assertFails(setDoc(doc(as('bob'), 'notifications/n2'), notification('n2', 'org', 'FOLLOWER', 'admin')));
  });
});

describe('reportes y moderación', () => {
  const report = (reporterId) => ({
    id: 'rep1', eventId: 'e1', reporterId, reason: 'spam_or_duplicate', detail: null, date: now(), resolved: false
  });

  test('un vecino reporta; solo el admin resuelve', async () => {
    await base();
    await assertSucceeds(setDoc(doc(as('bob'), 'reports/rep1'), report('bob')));
    await assertFails(updateDoc(doc(as('bob'), 'reports/rep1'), { resolved: true }));
    await assertSucceeds(updateDoc(doc(as('admin'), 'reports/rep1'), { resolved: true }));
  });

  test('motivo de reporte fuera del catálogo se rechaza', async () => {
    await base();
    await assertFails(setDoc(doc(as('bob'), 'reports/rep1'), { ...report('bob'), reason: 'me_cae_mal' }));
  });

  test('el historial de moderación solo lo crea el admin y rechazar exige motivo', async () => {
    await base();
    const review = (adminId, action, reason) => ({ id: 'm1', eventId: 'e1', adminId, action, reason, date: now() });
    await assertFails(setDoc(doc(as('bob'), 'moderationReviews/m1'), review('bob', 'VERIFICAR', null)));
    await assertFails(setDoc(doc(as('admin'), 'moderationReviews/m1'), review('admin', 'RECHAZAR', null)));
    await assertSucceeds(setDoc(doc(as('admin'), 'moderationReviews/m1'), review('admin', 'RECHAZAR', 'Spam')));
    await assertFails(updateDoc(doc(as('admin'), 'moderationReviews/m1'), { reason: 'Otro' }));
  });

  test('veredictos y resúmenes del asistente son solo del admin', async () => {
    await base();
    const verdict = { eventId: 'e1', recommendation: 'APROBAR', explanation: 'Cumple', eventFingerprint: 'a'.repeat(64), date: now() };
    await assertFails(setDoc(doc(as('bob'), 'eventVerdicts/e1'), verdict));
    await assertSucceeds(setDoc(doc(as('admin'), 'eventVerdicts/e1'), verdict));
    await assertFails(getDoc(doc(as('bob'), 'eventVerdicts/e1')));
    await assertFails(setDoc(doc(as('admin'), 'eventVerdicts/e1'), { ...verdict, eventFingerprint: 'corto' }));
  });
});
