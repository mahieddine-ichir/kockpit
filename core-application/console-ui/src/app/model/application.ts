export interface Application {
  name: string;
  label: string;
  route: string;
  appId: string[];
}

export interface AppColumn {
  name: string;
  label: string;
  renderer: string;
}

export class ConsoleMenuItem {
  parentId: string;
  id: string;
  label: string;
  route: string;
  app: string;
  domain: string;
  subDomain: string;
  env: string;
}

export class ConsoleServiceMenu {
  serviceId: string;
  menuItems: ConsoleMenuItem[];
}

export class ConsoleServiceConfig {
  serviceId: string;
  config: any;
}

export class ConsoleConfig {
  consoleServiceMenus: ConsoleServiceMenu[];
  consoleServiceConfigs: ConsoleServiceConfig[];
}

