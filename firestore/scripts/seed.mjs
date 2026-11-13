// Siembra los catálogos (categories, cities, placeSuggestions) con el Admin SDK, que no pasa por las reglas.
// Es idempotente: cada documento tiene id fijo y se sobrescribe con set().
//
//   Emulador:   npm run seed:emulator
//   Producción: GOOGLE_APPLICATION_CREDENTIALS=./service-account.json npm run seed
//               (o, en lugar de la variable, `gcloud auth application-default login`)
//
// El resto de colecciones no necesita crearse: Firestore las crea con el primer documento que escribe la app.

import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { initializeApp } from 'firebase-admin/app';
import { getFirestore, GeoPoint } from 'firebase-admin/firestore';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const readSeed = (name) => JSON.parse(readFileSync(join(root, 'seed', `${name}.json`), 'utf8'));
const projectId = process.env.GCLOUD_PROJECT ?? 'tribooo';

const catalogs = {
  categories: readSeed('categories').map((c) => ({ id: c.id, data: c })),
  // Id = nombre en minúsculas, como indica city.schema.json
  cities: readSeed('cities').map((name) => {
    const id = name.toLocaleLowerCase('es');
    return { id, data: { id, name } };
  }),
  placeSuggestions: readSeed('placeSuggestions').map(({ location, ...place }) => ({
    id: place.id,
    data: { ...place, location: new GeoPoint(location.latitude, location.longitude) }
  }))
};

const target = process.env.FIRESTORE_EMULATOR_HOST ? `emulador ${process.env.FIRESTORE_EMULATOR_HOST}` : `proyecto ${projectId}`;
console.log(`Sembrando catálogos en ${target}...`);

const db = getFirestore(initializeApp({ projectId }));
for (const [collection, docs] of Object.entries(catalogs)) {
  const batch = db.batch();
  docs.forEach(({ id, data }) => batch.set(db.collection(collection).doc(id), data));
  await batch.commit();
  console.log(`  ${collection}: ${docs.length} documentos`);
}
console.log('Listo.');
