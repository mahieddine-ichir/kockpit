import { useCallback } from 'react'
import { useState } from 'react'
import { useLocalStorage } from 'usehooks-ts'

export type StringHistory = {
  histArray: string[]
  currentIndex: number
}

export function useStringLocalStorageHistory({
  localStorageKey,
  initialValue,
}: {
  localStorageKey: string
  initialValue: string
}) {
  const [history, setHistory] = useLocalStorage<StringHistory>(
    localStorageKey,
    {
      histArray: [initialValue],
      currentIndex: 0,
    }
  )

  const setValue = useCallback(
    (newValue: string) => {
      if (newValue === history.histArray[history.currentIndex]) return
      setHistory((prevHistory) => {
        const updatedHistArray = [
          ...prevHistory.histArray.slice(0, prevHistory.currentIndex + 1),
        ]
        if (updatedHistArray.length > 50) {
          updatedHistArray.shift()
        }
        updatedHistArray.push(newValue)
        const newIndex = updatedHistArray.length - 1
        return { histArray: updatedHistArray, currentIndex: newIndex }
      })
    },
    [history.currentIndex, history.histArray]
  )

  const undo = useCallback(() => {
    if (history.currentIndex > 0) {
      setHistory((prevHistory) => ({
        ...prevHistory,
        currentIndex: prevHistory.currentIndex - 1,
      }))
    }
  }, [history.currentIndex])

  const redo = useCallback(() => {
    if (history.currentIndex < history.histArray.length - 1) {
      setHistory((prevHistory) => ({
        ...prevHistory,
        currentIndex: prevHistory.currentIndex + 1,
      }))
    }
  }, [history.currentIndex, history.histArray.length])

  return {
    value: history.histArray[history.currentIndex],
    setValue,
    undo,
    redo,
    canUndo: history.currentIndex > 0,
    canRedo: history.currentIndex < history.histArray.length - 1,
    history,
    setHistory,
  }
}
