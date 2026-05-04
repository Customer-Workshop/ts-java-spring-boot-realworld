import { test, expect } from '@playwright/test';

test.describe('Campaign Manager', () => {
  test('should redirect unauthenticated users to login', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveURL(/\/login/);
  });

  test('should display login form', async ({ page }) => {
    await page.goto('/login');
    await expect(page.locator('h1')).toContainText('Campaign Manager');
    await expect(page.locator('input[type="email"]')).toBeVisible();
    await expect(page.locator('input[type="password"]')).toBeVisible();
    await expect(page.locator('button[type="submit"]')).toContainText(
      'Sign In'
    );
  });

  test('should show error for invalid credentials', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="email"]', 'invalid@test.com');
    await page.fill('input[type="password"]', 'wrongpassword');
    await page.click('button[type="submit"]');
    await expect(page.locator('text=Invalid credentials')).toBeVisible({
      timeout: 10000,
    });
  });

  test('should show access denied page', async ({ page }) => {
    await page.goto('/access-denied');
    await expect(page.locator('text=Access Denied')).toBeVisible();
    await expect(
      page.locator('text=Marketing')
    ).toBeVisible();
  });
});
