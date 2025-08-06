import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AuthGuard} from './services/auth.guard';
import {LayoutComponent} from './core/layout/layout.component';
import {DetailComponent} from './pages/audit/detail/detail.component';
import {ServiceAuthorizedGuard} from './services/service-authorized-guard.service';
import {DeprecatedMarkerGuard} from './services/deprecated-marker-guard.service';
import {UsernotificationComponent} from './pages/usernotification/usernotification.component';
import {ManifestComponent} from './pages/manifest/manifest.component';
import {DashboardComponent} from './pages/dashboard/dashboard.component';

const routes: Routes = [
  {
    path: 'login',
    loadChildren: () => import('./pages/custom-pages/login/login.module').then(m => m.LoginModule),
  },
  {
    path: 'logout',
    loadChildren: () => import('./pages/custom-pages/logout/logout.module').then(m => m.LogoutModule),
  },
  {
    path: 'detail/:domain/:env/:id',
    component: LayoutComponent,
    canActivate: [DeprecatedMarkerGuard],
    children: [
      {
        path: '',
        canActivate: [AuthGuard, DeprecatedMarkerGuard],
        component: DetailComponent,
        loadChildren: () => import('./pages/audit/request.module').then(m => m.RequestModule)
      }
    ]
  },
  {
    path: 'notification',
    component: LayoutComponent,
    canActivate: [DeprecatedMarkerGuard],
    children: [
      {
        path: '',
        canActivate: [AuthGuard, DeprecatedMarkerGuard],
        component: UsernotificationComponent,
        loadChildren: () => import('./pages/usernotification/usernotification.module').then(m => m.UsernotificationModule)
      }
    ]
  },
  {
    path: 'manifest',
    component: LayoutComponent,
    canActivate: [DeprecatedMarkerGuard],
    children: [
      {
        path: '',
        canActivate: [AuthGuard, DeprecatedMarkerGuard],
        component: ManifestComponent,
        loadChildren: () => import('./pages/manifest/manifest.module').then(m => m.ManifestModule)
      }
    ]
  },
  {
    path: '',
    component: LayoutComponent,
    loadChildren: () => import('./pages/custom-pages/loading/loading.module').then(m => m.LoadingModule)
  },
  {
    path: 'topology/:domain/:env',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    loadChildren: () => import('./pages/topology/topology.module').then(m => m.TopologyModule)
  },
  {
    path: 'services',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    children: [
      {
        path: 'audit/dashboard',
        canActivate: [AuthGuard],
        component: DashboardComponent,
        loadChildren: () => import('./pages/dashboard/dashboard.module').then(m => m.DashboardModule)
      },
      {
        path: 'audit/:domain/:env/:auditViewName',
        canActivate: [AuthGuard, ServiceAuthorizedGuard],
        canActivateChild: [ServiceAuthorizedGuard],
        loadChildren: () => import('./pages/audit/request.module').then(m => m.RequestModule)
      },
      {
        path: 'sqsdlq/:domain/:env/:applicationId/:queueName',
        canActivate: [AuthGuard, ServiceAuthorizedGuard],
        loadChildren: () => import('./pages/queue/queues-messages.module').then(m => m.QueuesMessagesModule)
      },
      {
        path: 'cache/:domain/:env/:applicationId/:cacheName',
        canActivate: [AuthGuard, ServiceAuthorizedGuard],
        loadChildren: () => import('./pages/cache/cache.module').then(m => m.CacheModule)
      },
      {
        path: 'dynaconfig/:domain/:env/:applicationId',
        canActivate: [AuthGuard],
        loadChildren: () => import('./pages/dynaconfig/dynaconfig.module').then(m => m.DynaconfigModule)
      },
      {
        path: 'featureflipping/:domain/:env/:applicationId',
        canActivate: [AuthGuard],
        loadChildren: () => import('./pages/featureflipping/featureflipping.module').then(m => m.FeatureflippingModule)
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {enableTracing: false, useHash: true})],
  exports: [RouterModule],
  providers: []
})
export class RoutingModule {
}
