import { TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { AppComponent } from './app.component';
// Opcionális pluszok, ha kell router vagy HTTP a template-ben/injektálásban:
// import { provideRouter } from '@angular/router';
// import { provideHttpClient } from '@angular/common/http';

describe('AppComponent (standalone)', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      // providers: [provideRouter([]), provideHttpClient()],
      schemas: [NO_ERRORS_SCHEMA], // ignorálja az ismeretlen tageket/attribútumokat CI-ben
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });
});
