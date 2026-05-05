import { render } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';

import App from './app';

describe('App', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('should render successfully', () => {
    const { baseElement } = render(
      <BrowserRouter>
        <AuthProvider>
          <App />
        </AuthProvider>
      </BrowserRouter>,
    );
    expect(baseElement).toBeTruthy();
  });

  it('should redirect unauthenticated users to login', () => {
    const { getByText } = render(
      <BrowserRouter>
        <AuthProvider>
          <App />
        </AuthProvider>
      </BrowserRouter>,
    );
    expect(getByText(/sign in/i)).toBeTruthy();
  });
});
