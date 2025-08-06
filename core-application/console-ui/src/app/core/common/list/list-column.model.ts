export class ListColumn {
  name?: string;
  property?: string;
  visible = true;
  isModelProperty?: boolean;
  displayFn: any;
  fxGrow = 1;
  fxShrink = 1;
  resizable = true;
  width?: number;
  dashboard = false;
}
