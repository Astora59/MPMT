import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InviteUserModal } from './invite-user-modal';

describe('InviteUserModal', () => {
  let component: InviteUserModal;
  let fixture: ComponentFixture<InviteUserModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InviteUserModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InviteUserModal);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
