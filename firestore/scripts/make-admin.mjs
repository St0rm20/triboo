// Asigna el rol ADMIN (o lo retira con --revoke) al usuario con ese correo.
// Las reglas impiden que un cliente cambie su propio rol, así que se hace con el Admin SDK.
//
//   npm run make-admin -- persona@correo.com
//   npm run make-admin -- persona@correo.com --revoke
//
// El usuario debe haberse registrado antes en la app (necesita su documento en "users").

import { initializeApp } from 'firebase-admin/app';
import { getFirestore } from 'firebase-admin/firestore';

const email = process.argv.slice(2).find((arg) => !arg.startsWith('--'));
const role = process.argv.includes('--revoke') ? 'USER' : 'ADMIN';
if (!email) {
  console.error('Uso: npm run make-admin -- <correo> [--revoke]');
  process.exit(1);
}

const db = getFirestore(initializeApp({ projectId: process.env.GCLOUD_PROJECT ?? 'tribooo' }));
const snapshot = await db.collection('users').where('email', '==', email.trim()).limit(1).get();
if (snapshot.empty) {
  console.error(`No hay ningún documento en "users" con email ${email}.`);
  process.exit(1);
}
await snapshot.docs[0].ref.update({ role });
console.log(`${email} (${snapshot.docs[0].id}) ahora tiene rol ${role}.`);
