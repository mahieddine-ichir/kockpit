import { atom } from 'jotai'

export type ElementType =
    | 'Rule'
    | 'Action'
    | 'Predicate'
    | 'Event'
    | 'Transition'
    | 'ComplexGateway'

export const elementIdAtom = atom('')
export const elementNameAtom = atom('')
export const elementTypeAtom = atom<ElementType>('Rule')
export const elementDescriptionAtom = atom('')

export const enableTabSetStripAtom = atom(false)
