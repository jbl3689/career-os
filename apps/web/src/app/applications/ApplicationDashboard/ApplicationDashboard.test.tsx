import { fireEvent, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  createApplication,
  listApplications,
} from "@/lib/applications-api";
import type { JobApplication } from "@/lib/applications-api";
import { renderWithQuery } from "@/test/render-with-query";
import { ApplicationDashboard } from "./ApplicationDashboard";

vi.mock("@/lib/applications-api", async (importOriginal) => {
  const actual = await importOriginal<
    typeof import("@/lib/applications-api")
  >();

  return {
    ...actual,
    createApplication: vi.fn(),
    listApplications: vi.fn(),
  };
});

const application: JobApplication = {
  id: 1,
  companyName: "Acme Ltd",
  roleTitle: "Software Engineer",
  status: "APPLIED",
  applicationDate: "2026-07-16",
  notes: "Applied through the company website.",
  lastActivityDate: "2026-07-16",
};

describe("ApplicationDashboard", () => {
  beforeEach(() => {
    vi.mocked(createApplication).mockReset();
    vi.mocked(listApplications).mockReset();
  });

  it("shows an empty state when there are no applications", async () => {
    vi.mocked(listApplications).mockResolvedValue([]);

    renderWithQuery(<ApplicationDashboard />);

    expect(screen.getByText("Loading applications…")).toBeDefined();
    expect(await screen.findByText("No applications yet")).toBeDefined();
  });

  it("renders applications returned by the API", async () => {
    vi.mocked(listApplications).mockResolvedValue([application]);

    renderWithQuery(<ApplicationDashboard />);

    const applicationLink = await screen.findByRole("link", {
      name: "Software Engineer",
    });
    expect(applicationLink.getAttribute("href")).toBe("/applications/1");
    expect(screen.getByText("Acme Ltd")).toBeDefined();
    expect(screen.getByText("Applied", { selector: "span" })).toBeDefined();
  });

  it("creates an application and adds it to the list", async () => {
    vi.mocked(listApplications).mockResolvedValue([]);
    vi.mocked(createApplication).mockResolvedValue(application);

    renderWithQuery(<ApplicationDashboard />);
    await screen.findByText("No applications yet");

    fireEvent.change(screen.getByLabelText("Company name"), {
      target: { value: "Acme Ltd" },
    });
    fireEvent.change(screen.getByLabelText("Role title"), {
      target: { value: "Software Engineer" },
    });
    fireEvent.change(screen.getByLabelText("Application date"), {
      target: { value: "2026-07-16" },
    });
    fireEvent.change(screen.getByLabelText("Notes"), {
      target: { value: "Applied through the company website." },
    });
    fireEvent.click(
      screen.getByRole("button", { name: "Add application" }),
    );

    await waitFor(() => {
      expect(createApplication).toHaveBeenCalledWith({
        companyName: "Acme Ltd",
        roleTitle: "Software Engineer",
        status: "APPLIED",
        applicationDate: "2026-07-16",
        notes: "Applied through the company website.",
      });
    });
    expect(
      await screen.findByRole("link", { name: "Software Engineer" }),
    ).toBeDefined();
    expect(screen.getByText("Software Engineer was added.")).toBeDefined();
  });

  it("shows an error state when applications cannot be loaded", async () => {
    vi.mocked(listApplications).mockRejectedValue(new Error("API unavailable"));

    renderWithQuery(<ApplicationDashboard />);

    expect(
      await screen.findByText("Applications could not be loaded."),
    ).toBeDefined();
    expect(screen.getByRole("button", { name: "Try again" })).toBeDefined();
  });
});
