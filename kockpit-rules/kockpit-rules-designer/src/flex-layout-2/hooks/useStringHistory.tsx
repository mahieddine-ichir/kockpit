import { useCallback } from 'react'
import { useState } from 'react'

export function useStringHistory(initialValue: string = '') {
  const [history, setHistory] = useState<{
    histArray: string[]
    currentIndex: number
  }>({
    histArray: [initialValue],
    currentIndex: 0,
  })

  const setValue = useCallback((newValue: string) => {
    setHistory((prevHistory) => {
      const updatedHistArray = [
        ...prevHistory.histArray.slice(0, prevHistory.currentIndex + 1),
      ]
      updatedHistArray.push(newValue)
      const newIndex = updatedHistArray.length - 1
      return { histArray: updatedHistArray, currentIndex: newIndex }
    })
  }, [])

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
  }
}
