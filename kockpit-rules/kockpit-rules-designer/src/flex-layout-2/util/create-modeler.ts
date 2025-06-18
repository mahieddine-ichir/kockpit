import Modeler from 'bpmn-js/lib/Modeler'

import PaletteProvider, {
  Palette,
  Create,
  ElementFactory,
  Translate,
  PaletteEntries,
} from 'bpmn-js/lib/features/palette/PaletteProvider'

import ContextPadProvider, {
  ContextPad,
  ContextPadEntries,
  Element,
} from 'bpmn-js/lib/features/context-pad/ContextPadProvider'
import Modeling from 'bpmn-js/lib/features/modeling/Modeling'

export function createModeler(container: string | HTMLElement) {
  return createModelerWithCustomPalette(container)
  // return new BpmnJS({ container: '#canvas' })
}

function createModelerWithCustomPalette(container: string | HTMLElement) {
  // Define the custom palette provider
  class CustomPalette implements PaletteProvider {
    private _create: Create
    private _elementFactory: ElementFactory
    private _translate: Translate
    static $inject: Array<
        'palette' | 'create' | 'elementFactory' | 'translate'
    > = []

    constructor(
        palette: Palette,
        create: Create,
        elementFactory: ElementFactory,
        translate: Translate
    ) {
      this._create = create
      this._elementFactory = elementFactory
      this._translate = translate

      palette.registerProvider(this)
    }

    getPaletteEntries(): PaletteEntries {
      return (entries: Record<string, unknown>) => {
        // const tools = [
        //   'hand-tool',
        //   'lasso-tool',
        //   'space-tool',
        //   'global-connect-tool',
        //   'tool-separator',
        //   'create.start-event',
        //   'create.intermediate-event',
        //   'create.end-event',
        //   'create.exclusive-gateway',
        //   'create.task',
        //   'create.data-object',
        //   'create.data-store',
        //   'create.subprocess-expanded',
        //   'create.participant-expanded',
        //   'create.group',
        // ]
        const toolsToKeep = [
          'global-connect-tool',
          'create.exclusive-gateway',
          'create.task',
          'create.start-event',
          'create.end-event',
        ]
        Object.keys(entries).forEach(function (key) {
          if (!toolsToKeep.includes(key)) {
            delete entries[key]
          }
        })
        entries['create.complex-gateway'] = {
          group: 'gateway',
          className: 'bpmn-icon-gateway-complex',
          title: 'Create Complex Gateway',
          action: {
            click: () => this._createGateway('bpmn:ComplexGateway'),
            dragstart: () => this._createGateway('bpmn:ComplexGateway'),
          },
        };
        return entries
      }
    }
    private _createGateway(type: string) {
      const shape = this._elementFactory.createShape({ type });
      this._create.start(event, shape);
    }
  }

  // Include dependencies in constructor
  CustomPalette.$inject = ['palette', 'create', 'elementFactory', 'translate']

  class CustomContextPad implements ContextPadProvider {
    private _modeling: Modeling
    private _elementFactory: ElementFactory
    private _create: Create
    static $inject: Array<
        'contextPad' | 'modeling' | 'elementFactory' | 'create'
    > = []
    constructor(
        contextPad: ContextPad,
        modeling: Modeling,
        elementFactory: ElementFactory,
        create: Create
    ) {
      this._modeling = modeling
      this._elementFactory = elementFactory
      this._create = create

      contextPad.registerProvider(this)
    }

    getContextPadEntries(element: Element): ContextPadEntries {
      return (entries: Record<string, unknown>) => {
        // const allEntries = [
        //   'append.end-event',
        //   'append.gateway',
        //   'append.append-task',
        //   'append.intermediate-event',
        //   'replace',
        //   'append.text-annotation',
        //   'connect',
        //   'delete',
        // ]
        const newEntries = {
          ...entries,
          'append.complex-gateway': {
            group: 'model',
            className: 'bpmn-icon-gateway-complex',
            title: 'Append Complex Gateway',
            action: {
              click: () => this._appendComplexGateway(element),
            },
          },
        }
        delete newEntries['append.intermediate-event']
        delete newEntries['append.text-annotation']
        delete newEntries['replace']
        return newEntries
      }
    }
    private _appendComplexGateway(element: Element) {
      const complexGateway = this._elementFactory.createShape({
        type: 'bpmn:ComplexGateway',
      });
      this._modeling.appendShape(element, complexGateway);
    }
  }

  CustomContextPad.$inject = [
    'contextPad',
    'modeling',
    'elementFactory',
    'create',
  ]


  const modeler = new Modeler({
    container: container,
    additionalModules: [
      {
        __init__: ['customPalette', 'customContextPad'],
        customPalette: ['type', CustomPalette],
        customContextPad: ['type', CustomContextPad],
      },
    ],
  })
  return modeler
}
