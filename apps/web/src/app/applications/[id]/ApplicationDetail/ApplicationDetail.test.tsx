import { fireEvent, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  ApiRequestError,
  getApplication,
  updateApplication,
} from "@/lib/applications-api";
import type { JobApplication } from "@/lib/applications-api";
import { renderWithQuery } from "@/test/render-with-query";
import { ApplicationDetail } from "./ApplicationDetail";

vi.mock("@/lib/applications-api", async (importOriginal) => {
  const actual = await importOriginal<
    typeof import("@/lib/applications-api")
  >();

  return {
    ...actual,
    getApplication: vi.fn(),
    updateApplication: vi.fn(),
  };
});

const application: JobApplication = {
  id: 1,
  companyName: "Acme Ltd",
  roleTitle: "Software Engineer",
  status: "APPLIED",
  applicationDate: "2026-07-16",
  source: "LinkedIn job post",
  notes: "Applied through the company website.",
  lastActivityDate: "2026-07-16",
};

describe("ApplicationDetail", () => {
  beforeEach(() => {
    vi.mocked(getApplication).mockReset();
    vi.mocked(updateApplication).mockReset();
  });

  it("loads and displays an application", async () => {
    vi.mocked(getApplication).mockResolvedValue(application);

    renderWithQuery(<ApplicationDetail applicationId={1} />);

    expect(screen.getByText("Loading application…")).toBeDefined();
    expect(
      await screen.findByRole("heading", { name: "Software Engineer" }),
    ).toBeDefined();
    expect(screen.getByText("Acme Ltd")).toBeDefined();
    expect(screen.getAllByText("LinkedIn job post")).toHaveLength(2);
    expect(screen.getByDisplayValue(application.notes)).toBeDefined();
  });

  it("updates status and notes", async () => {
    vi.mocked(getApplication).mockResolvedValue(application);
    vi.mocked(updateApplication).mockResolvedValue({
      ...application,
      status: "INTERVIEWING",
      notes: "First interview booked.",
      lastActivityDate: "2026-07-20",
    });

    renderWithQuery(<ApplicationDetail applicationId={1} />);
    await screen.findByRole("heading", { name: "Software Engineer" });

    fireEvent.change(screen.getByLabelText("Status"), {
      target: { value: "INTERVIEWING" },
    });
    fireEvent.change(screen.getByLabelText("Notes"), {
      target: { value: "First interview booked." },
    });
    fireEvent.change(screen.getByLabelText("Application source"), {
      target: { value: "Indeed" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Save changes" }));

    await waitFor(() => {
      expect(updateApplication).toHaveBeenCalledWith(1, {
        status: "INTERVIEWING",
        source: "Indeed",
        notes: "First interview booked.",
      });
    });
    expect(await screen.findByText("Application updated.")).toBeDefined();
    expect(screen.getByText("Interviewing", { selector: "span" })).toBeDefined();
  });

  it("shows a not-found state for an unknown application", async () => {
    vi.mocked(getApplication).mockRejectedValue(
      new ApiRequestError("Job application 999 was not found", 404),
    );

    renderWithQuery(<ApplicationDetail applicationId={999} />);

    expect(
      await screen.findByText("This application could not be found."),
    ).toBeDefined();
    expect(screen.getByRole("link", { name: "Back to applications" })).toBeDefined();
  });

  it("rejects an invalid application ID without calling the API", () => {
    renderWithQuery(<ApplicationDetail applicationId={Number.NaN} />);

    expect(screen.getByText("This application ID is invalid.")).toBeDefined();
    expect(getApplication).not.toHaveBeenCalled();
  });
});
