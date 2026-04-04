import { Component, EventEmitter, HostBinding, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';
import { NavItem, UserSession } from '../../models/index';
import { NAV_ITEMS } from '../../../core/config/nav-items.config';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss'],
})
export class SidebarComponent implements OnInit, OnDestroy {
  @Input() isOpen = false;
  @Output() closed = new EventEmitter<void>();

  @HostBinding('class.drawer-open') get drawerOpen() { return this.isOpen; }

  navItems: NavItem[] = [];
  currentUser$!: Observable<UserSession | null>;
  private destroy$ = new Subject<void>();

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.currentUser$ = this.authService.currentUser$;
    this.authService.role$.pipe(takeUntil(this.destroy$)).subscribe((role) => {
      this.navItems = role ? (NAV_ITEMS[role as keyof typeof NAV_ITEMS] ?? []) : [];
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  logout(): void {
    this.authService.logout();
  }
}
