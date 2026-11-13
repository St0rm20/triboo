// Genera firestore.rules a partir de rules/access.rules (permisos, escritos a mano)
// y de schemas/*.schema.json (forma de cada documento, convertida en funciones isValid<Title>).
//
//   node scripts/generate-rules.mjs          escribe firestore.rules
//   node scripts/generate-rules.mjs --check  falla si firestore.rules no está al día (para CI)

import { readFileSync, readdirSync, writeFileSync, existsSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const schemasDir = join(root, 'schemas');
const templatePath = join(root, 'rules', 'access.rules');
const outputPath = join(root, 'firestore.rules');
const MARKER = '// <<GENERATED_VALIDATORS>>';

const quote = (value) => `'${String(value).replace(/\\/g, '\\\\').replace(/'/g, "\\'")}'`;
const list = (values) => `[${values.map(quote).join(', ')}]`;

/** Tipos permitidos por la propiedad; "null" se trata aparte para generar `x == null || ...`. */
function typesOf(schema) {
  const types = Array.isArray(schema.type) ? schema.type : [schema.type];
  return { nullable: types.includes('null'), types: types.filter((t) => t !== 'null') };
}

/** Expresión de reglas que valida el valor en `path` contra `schema` (sin contemplar null). */
function valueCheck(path, schema, type) {
  const checks = [];
  if (schema['x-firestore-type'] === 'timestamp') {
    checks.push(`${path} is timestamp`);
  } else if (schema['x-firestore-type'] === 'geopoint') {
    checks.push(`${path} is latlng`);
  } else if (type === 'string') {
    checks.push(`${path} is string`);
    if (schema.minLength !== undefined) checks.push(`${path}.size() >= ${schema.minLength}`);
    if (schema.maxLength !== undefined) checks.push(`${path}.size() <= ${schema.maxLength}`);
    if (schema.pattern) checks.push(`${path}.matches(${quote(schema.pattern)})`);
    if (schema.format === 'email') checks.push(`${path}.matches('^[^@\\\\s]+@[^@\\\\s]+$')`);
  } else if (type === 'integer') {
    checks.push(`${path} is int`);
  } else if (type === 'number') {
    checks.push(`${path} is number`);
  } else if (type === 'boolean') {
    checks.push(`${path} is bool`);
  } else if (type === 'array') {
    // Las reglas no pueden recorrer listas: solo se valida el tipo y el tamaño
    checks.push(`${path} is list`);
    if (schema.maxItems !== undefined) checks.push(`${path}.size() <= ${schema.maxItems}`);
  } else if (type === 'object') {
    checks.push(`${path} is map`, ...objectChecks(path, schema));
  } else {
    throw new Error(`Tipo no soportado en ${path}: ${type}`);
  }

  if (type === 'integer' || type === 'number') {
    if (schema.minimum !== undefined) checks.push(`${path} >= ${schema.minimum}`);
    if (schema.maximum !== undefined) checks.push(`${path} <= ${schema.maximum}`);
  }
  const enumValues = (schema.enum ?? []).filter((v) => v !== null);
  if (enumValues.length) checks.push(`${path} in ${list(enumValues)}`);
  return checks;
}

function propertyCheck(path, schema) {
  const { nullable, types } = typesOf(schema);
  const alternatives = types.map((type) => valueCheck(path, schema, type).join(' && '));
  const typed = alternatives.length === 1 ? alternatives[0] : alternatives.map((a) => `(${a})`).join(' || ');
  return nullable ? `(${path} == null || (${typed}))` : `(${typed})`;
}

/** Claves obligatorias, ninguna clave extra y cada propiedad con su tipo. */
function objectChecks(path, schema) {
  const properties = schema.properties ?? {};
  const required = schema.required ?? [];
  const checks = [`${path}.keys().hasAll(${list(required)})`];
  if (schema.additionalProperties === false) checks.push(`${path}.keys().hasOnly(${list(Object.keys(properties))})`);
  for (const [name, property] of Object.entries(properties)) {
    const check = propertyCheck(`${path}.${name}`, property);
    checks.push(required.includes(name) ? check : `(!('${name}' in ${path}) || ${check})`);
  }
  return checks;
}

function validatorFor(schema, file) {
  if (!schema.title || !schema['x-firestore-collection']) throw new Error(`${file}: falta title o x-firestore-collection`);
  const body = objectChecks('data', schema).join('\n        && ');
  return [
    `    // ${schema['x-firestore-collection']} (${file})`,
    `    function isValid${schema.title}(data) {`,
    `      return ${body};`,
    `    }`
  ].join('\n');
}

const files = readdirSync(schemasDir).filter((f) => f.endsWith('.schema.json')).sort();
const validators = files
  .map((file) => validatorFor(JSON.parse(readFileSync(join(schemasDir, file), 'utf8')), file))
  .join('\n\n');

const template = readFileSync(templatePath, 'utf8');
if (!template.includes(MARKER)) throw new Error(`rules/access.rules no contiene ${MARKER}`);

const header = [
  '// ARCHIVO GENERADO: no lo edites a mano.',
  '// Permisos: firestore/rules/access.rules · Forma de los documentos: firestore/schemas/*.schema.json',
  '// Regenerar: npm run rules:gen (desde firestore/)',
  ''
].join('\n');
const output = header + template.replace(MARKER, () => `// ---------- Validadores generados desde los JSON Schema ----------\n\n${validators}`);

if (process.argv.includes('--check')) {
  const current = existsSync(outputPath) ? readFileSync(outputPath, 'utf8') : '';
  if (current !== output) {
    console.error('firestore.rules está desactualizado: ejecuta `npm run rules:gen`.');
    process.exit(1);
  }
  console.log('firestore.rules está al día.');
} else {
  writeFileSync(outputPath, output);
  console.log(`firestore.rules generado con ${files.length} validadores.`);
}
