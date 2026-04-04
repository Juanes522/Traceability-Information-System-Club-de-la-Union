import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { UnauthorizedComponent } from './components/unauthorized/unauthorized.component';
import { ToastComponent } from './components/toast/toast.component';

@NgModule({
  declarations: [SidebarComponent, UnauthorizedComponent, ToastComponent],
  imports: [CommonModule, RouterModule],
  exports: [SidebarComponent, UnauthorizedComponent, ToastComponent],
})
export class SharedModule {}
