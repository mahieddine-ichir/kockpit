export interface UserNotification {
  serviceId: string;
  description: string;
  env: string;
  domain: string;
  date: number;
  level: string;
  applicationId: string;
  id: string;
  read: boolean;
}
