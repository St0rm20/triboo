# Firestore de Tribooo

Reglas de seguridad, índices, catálogos y pruebas de Firestore. Se requiere Node 20 o superior y Java 21 o superior (para el emulador).

```bash
cd firestore
npm install
```

## Estructura

| Ruta | Contenido |
|---|---|
| `schemas/*.schema.json` | Forma de cada documento (fuente de verdad, sincronizada con los DTO de `data/remote`) |
| `rules/access.rules` | Permisos escritos a mano: quién puede leer, crear, editar y borrar |
| `firestore.rules` | **Generado** a partir de los dos anteriores; no se edita a mano |
| `seed/*.json` | Catálogos: `categories`, `cities` y `placeSuggestions` |
| `tests/rules.test.mjs` | Pruebas de las reglas contra el emulador |

## Comandos

| Comando | Qué hace |
|---|---|
| `npm run rules:gen` | Regenera `firestore.rules` desde los schemas y `rules/access.rules` |
| `npm test` | Comprueba que `firestore.rules` esté al día y ejecuta las pruebas en el emulador |
| `npm run emulators` | Levanta los emuladores de Firestore y Auth (interfaz en http://127.0.0.1:4000) |
| `npm run seed:emulator` | Siembra los catálogos en el emulador que ya está corriendo |
| `npm run deploy` | Ejecuta las pruebas y, si pasan, publica las reglas y los índices en el proyecto `tribooo` |
| `npm run seed` | Siembra los catálogos en producción |
| `npm run make-admin -- <correo>` | Asigna el rol ADMIN (`--revoke` lo retira) |
| `npm run setup` | `deploy` y luego `seed`: deja un proyecto nuevo listo |

Las demás colecciones no se crean a mano: Firestore crea cada una con el primer documento que escribe la app.

## Credenciales para producción

`deploy` usa la sesión de la CLI (`npx firebase login`). `seed` y `make-admin` usan el Admin SDK, que necesita una de estas dos opciones:

- `gcloud auth application-default login`
- `GOOGLE_APPLICATION_CREDENTIALS=./service-account.json` (clave de cuenta de servicio; está en `.gitignore`)

## Cambiar el modelo

1. Edita el DTO y su `schemas/*.schema.json`; si cambian los permisos, edita también `rules/access.rules`.
2. Ejecuta `npm run rules:gen` y luego `npm test`; agrega una prueba si hay un permiso nuevo.
3. Ejecuta `npm run deploy`.
