import { convertJson2ToJson3, JSON3 } from './convert-json2-to-json3'
import { convertXmlToJson2 } from './convert-xml-to-json2'
import { ProblemsDispatch } from '../../components/ProblemsTab'
import { JSON2 } from './util'

export function convertXmlToJson(xml: string): JSON3 {
  const json2 = convertXmlToJson2(xml)
  const json3 = convertJson2ToJson3(json2)
  return json3
}

function dispatchJsonProblems(json2: JSON2, dispatch: ProblemsDispatch) {
  for (const link of json2.links) {
    if (link.source === link.target) {
      dispatch({
        component: 'BpmnCanvas',
        type: 'error',
        isActive: true,
        name: 'Transition has the same source and target',
        message: 'This is going to cause an infinite loop.',
      })
      // throw new Error('Transition has the same source and target')
    }
  }
}
