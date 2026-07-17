import { fireEvent, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { applicationQueryKeys } from "@/lib/application-query-keys";
import { getCurrentUser, logout } from "@/lib/auth-api";
import { renderWithQuery } from "@/test/render-with-query";
import { RequireAuthentication } from "../RequireAuthentication";
import { AuthStatus } from "./AuthStatus";

vi.mock("@/lib/auth-api", () => ({
  getCurrentUser: vi.fn(),
  logout: vi.fn(),
}));

describe("AuthStatus", () => {
  beforeEach(() => {
    vi.mocked(getCurrentUser).mockReset();
    vi.mocked(logout).mockReset();
  });

  it("removes protected and cached application data after sign out", async () => {
    vi.mocked(getCurrentUser).mockResolvedValue({
      id: 1,
      email: "developer@example.com",
      displayName: "Career OS Developer",
      avatarUrl: null,
    });
    vi.mocked(logout).mockResolvedValue();

    const { queryClient } = renderWithQuery(
      <>
        <AuthStatus />
        <RequireAuthentication>
          <p>Private tracker</p>
        </RequireAuthentication>
      </>,
    );
    queryClient.setQueryData(applicationQueryKeys.all, [{ id: 1 }]);

    expect(await screen.findByText("Private tracker")).toBeDefined();
    fireEvent.click(screen.getByRole("button", { name: "Sign out" }));

    await waitFor(() => {
      expect(screen.queryByText("Private tracker")).toBeNull();
    });
    expect(
      screen.getByRole("link", { name: "Continue with Google" }),
    ).toBeDefined();
    expect(queryClient.getQueryData(applicationQueryKeys.all)).toBeUndefined();
  });
});
