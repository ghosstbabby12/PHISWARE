// Fails when a key exists in one locale but not the other (functions count as keys).
import { createSourceFile, ScriptTarget, SyntaxKind } from 'typescript'
import { readFileSync } from 'node:fs'

const src = readFileSync(new URL('../src/i18n/translations.ts', import.meta.url), 'utf8')
const sf = createSourceFile('translations.ts', src, ScriptTarget.Latest, true)

function findStrings() {
  for (const st of sf.statements) {
    if (st.kind === SyntaxKind.VariableStatement) {
      for (const decl of st.declarationList.declarations) {
        if (decl.name.getText() === 'STRINGS' && decl.initializer) return decl.initializer
      }
    }
  }
  throw new Error('STRINGS not found')
}

function keysOf(objLiteral, prefix, out) {
  for (const prop of objLiteral.properties) {
    if (prop.kind !== SyntaxKind.PropertyAssignment) continue
    const name = prop.name.getText()
    const path = prefix ? prefix + '.' + name : name
    out.push(path)
    if (prop.initializer && prop.initializer.kind === SyntaxKind.ObjectLiteralExpression) {
      keysOf(prop.initializer, path, out)
    }
  }
  return out
}

const rootRaw = findStrings()
const root = rootRaw.kind === SyntaxKind.AsExpression ? rootRaw.expression : rootRaw
const locales = {}
for (const prop of root.properties) {
  if (prop.kind === SyntaxKind.PropertyAssignment) {
    locales[prop.name.getText()] = new Set(keysOf(prop.initializer, '', []))
  }
}

const es = locales.es ?? new Set()
const en = locales.en ?? new Set()
const missingInEn = [...es].filter(k => !en.has(k))
const missingInEs = [...en].filter(k => !es.has(k))

if (missingInEn.length || missingInEs.length) {
  for (const k of missingInEn) console.error('Missing in en: ' + k)
  for (const k of missingInEs) console.error('Missing in es: ' + k)
  process.exit(1)
}
console.log('i18n OK: ' + es.size + ' keys in both locales')
