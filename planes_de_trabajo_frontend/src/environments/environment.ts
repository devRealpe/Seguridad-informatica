// Archivo de entorno (desarrollo) para planes_de_trabajo
// Reutiliza la configuración compartida en libs y añade el flag `production: false`
import { environment as sharedEnv } from '../../../../libs/shared/shared-environments/lib/environments';

export const environment = {
  ...sharedEnv,
  production: false,
  
  // ✅ Sobrescribir URLs para usar proxy en desarrollo (rutas relativas)
  authApi: '/api',
  generalApi: '/api/general',
  generalMongoDBApi: '/api/general-mongodb',
  smtpApi: '/api/smtp',
  apiPlanesDeTraba: '/api/planes-de-trabajo',
  apiptlocal: '/api/planes-de-trabajo-local',
  apipt: '/api/oracle',
};
