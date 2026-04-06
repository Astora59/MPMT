import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { InviteUserModalComponent } from './invite-user-modal';

describe('InviteUserModalComponent', () => {
  let component: InviteUserModalComponent;
  let fixture: ComponentFixture<InviteUserModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InviteUserModalComponent],
      providers: [
      provideRouter([]),
      provideHttpClient()
    ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InviteUserModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
