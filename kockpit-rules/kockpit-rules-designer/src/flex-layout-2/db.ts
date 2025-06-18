import Dexie, { EntityTable } from 'dexie'
import { StringHistory } from './hooks/useStringLocalStorageHistory'
import { IJsonModel } from 'flexlayout-react'

export interface Rule {
  id: number
  name: string
  description: string
  xmlHistory: StringHistory
  jsonStr: string
  layout: IJsonModel
}

export const db = new Dexie('db_rules') as Dexie & {
  rules: EntityTable<Rule, 'id'>
}

db.version(1).stores({
  rules: '++id, name, description, xmlHistory, jsonStr, layout',
})
