// Archivo de entorno (producción) para planes_de_trabajo
import { environment as sharedEnv } from '../../../../libs/shared/shared-environments/lib/environments';

export const environment = {
  ...sharedEnv,
  production: true,
};
