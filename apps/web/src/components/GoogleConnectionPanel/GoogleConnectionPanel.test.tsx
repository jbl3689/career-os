import { fireEvent, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  disconnectGoogleConnection,
  getGoogleConnection,
} from "@/lib/google-connection-api";
import { renderWithQuery } from "@/test/render-with-query";
import { GoogleConnectionPanel } from "./GoogleConnectionPanel";

vi.mock("@/lib/google-connection-api", () => ({
  getGoogleConnection: vi.fn(),
  disconnectGoogleConnection: vi.fn(),
}));

describe("GoogleConnectionPanel", () => {
  beforeEach(() => {
    vi.mocked(getGoogleConnection).mockReset();
    vi.mocked(disconnectGoogleConnection).mockReset();
  });

  it("offers a separate Gmail connection when Gmail is not connected", async () => {
    vi.mocked(getGoogleConnection).mockResolvedValue({
      connected: false,
      gmailAddress: null,
      connectedAt: null,
    });

    renderWithQuery(<GoogleConnectionPanel />);

    const connectLink = await screen.findByRole("link", {
      name: "Connect Gmail",
    });
    expect(connectLink.getAttribute("href")).toBe(
      "/oauth2/authorization/google-gmail",
    );
  });

  it("shows the mailbox and disconnects it", async () => {
    vi.mocked(getGoogleConnection).mockResolvedValue({
      connected: true,
      gmailAddress: "developer@example.com",
      connectedAt: "2026-07-20T10:00:00Z",
    });
    vi.mocked(disconnectGoogleConnection).mockResolvedValue();

    renderWithQuery(<GoogleConnectionPanel />);

    expect(
      await screen.findByText("Connected as developer@example.com"),
    ).toBeDefined();
    fireEvent.click(
      screen.getByRole("button", { name: "Disconnect Gmail" }),
    );

    await waitFor(() => {
      expect(disconnectGoogleConnection).toHaveBeenCalledOnce();
    });
    expect(await screen.findByRole("link", { name: "Connect Gmail" })).toBeDefined();
  });
});
