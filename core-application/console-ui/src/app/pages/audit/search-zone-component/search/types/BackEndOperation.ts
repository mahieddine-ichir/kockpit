export class BackEndOperation {
  name: string;
  tooltip: string;

  constructor(name: string, tooltip: string) {
    this.name = name;
    this.tooltip = tooltip;
  }

  static toDto(x) {
    return x.name;
  }
}
