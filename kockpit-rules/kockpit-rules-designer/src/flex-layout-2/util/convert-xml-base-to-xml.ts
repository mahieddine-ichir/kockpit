import { ProblemsDispatch } from '../components/ProblemsTab'

export function convertBpmnXmlBaseToDiagramXml(xml: string) {
  // Regular expression to match the id pattern
  const idPattern = /\b(Process|Activity|Flow|Gateway|ComplexGateway)_(\w+)/g

  // Replace function to update the id based on the first part
  const updatedXml = xml.replace(idPattern, (match, p1, p2) => {
    switch (p1) {
      case 'Process':
        return `Rule_${p2}`
      case 'Activity':
        return `Action_${p2}`
      case 'Flow':
        return `Transition_${p2}`
      case 'Gateway':
        return `Predicate_${p2}`
      case 'ComplexGateway':
        return `ComplexGateway_${p2}`
      default:
        return match // In case of unexpected match, return the original
    }
  })

  return updatedXml
}
