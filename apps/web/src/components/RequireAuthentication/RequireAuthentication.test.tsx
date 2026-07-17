import { screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { ApiRequestError } from "@/lib/api-client";
import { getCurrentUser } from "@/lib/auth-api";
import { renderWithQuery } from "@/test/render-with-query";
import { RequireAuthentication } from "./RequireAuthentication";

vi.mock("@/lib/auth-api", () => ({
  getCurrentUser: vi.fn(),
}));

describe("RequireAuthentication", () => {
  beforeEach(() => {
    vi.mocked(getCurrentUser).mockReset();
  });

  it("renders protected content for a signed-in user", async () => {
    vi.mocked(getCurrentUser).mockResolvedValue({
      id: 1,
      email: "developer@example.com",
      displayName: "Career OS Developer",
      avatarUrl: null,
    });

    renderWithQuery(
      <RequireAuthentication>
        <p>Private tracker</p>
      </RequireAuthentication>,
    );

    expect(screen.getByText("Checking your session…")).toBeDefined();
    expect(await screen.findByText("Private tracker")).toBeDefined();
  });

  it("offers Google sign-in when there is no authenticated session", async () => {
    vi.mocked(getCurrentUser).mockRejectedValue(
      new ApiRequestError("Unauthorized", 401),
    );

    renderWithQuery(
      <RequireAuthentication>
        <p>Private tracker</p>
      </RequireAuthentication>,
    );

    const signInLink = await screen.findByRole("link", {
      name: "Continue with Google",
    });
    expect(signInLink.getAttribute("href")).toBe(
      "/oauth2/authorization/google",
    );
    expect(screen.queryByText("Private tracker")).toBeNull();
  });

  it("distinguishes an unavailable API from a signed-out user", async () => {
    vi.mocked(getCurrentUser).mockRejectedValue(new Error("API unavailable"));

    renderWithQuery(
      <RequireAuthentication>
        <p>Private tracker</p>
      </RequireAuthentication>,
    );

    expect(
      await screen.findByRole("heading", {
        name: "Authentication unavailable",
      }),
    ).toBeDefined();
    expect(
      screen.queryByRole("link", { name: "Continue with Google" }),
    ).toBeNull();
  });
});
