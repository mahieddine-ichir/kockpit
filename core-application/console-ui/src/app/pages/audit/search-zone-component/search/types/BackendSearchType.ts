import {BackEndOperation} from "./BackEndOperation";

export class BackendSearchType {
  name: string;
  operations: BackEndOperation[];

  static toDto(x): string {
    return x.name;
  }
}
