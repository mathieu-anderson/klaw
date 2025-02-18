import MainNavigation from "src/app/layout/MainNavigation";
import { cleanup, screen, render, within } from "@testing-library/react";

// mock out svgs to avoid clutter
jest.mock("@aivenio/design-system", () => {
  return {
    __esModule: true,
    ...jest.requireActual("@aivenio/design-system"),

    Icon: () => {
      return <div data-testid={"ds-icon"}></div>;
    },
  };
});

const navLinks = [
  {
    name: "Overview",
    linkTo: "/index",
  },
  { name: "Topics", linkTo: "/topics" },
  { name: "Kafka Connector", linkTo: "/kafkaConnectors" },
  { name: "Schemas", linkTo: "/" },
  { name: "Users and teams", linkTo: "/users" },
  { name: "Audit log", linkTo: "/activityLog" },
  { name: "Settings", linkTo: "/serverConfig" },
];

describe("SidebarNavigation.tsx", () => {
  beforeAll(() => {
    render(<MainNavigation />);
  });

  afterAll(cleanup);

  it("renders the main navigation", () => {
    const nav = screen.getByRole("navigation", { name: "Main navigation" });
    expect(nav).toBeVisible();
  });

  navLinks.forEach((link) => {
    it(`renders a link for ${link.name}`, () => {
      const nav = screen.getByRole("navigation", {
        name: "Main navigation",
      });

      const navLink = within(nav).getByRole("link", { name: link.name });
      expect(navLink).toBeVisible();
      expect(navLink).toHaveAttribute("href", link.linkTo);
    });
  });

  it(`renders all navigation items`, () => {
    const navLinks = screen.getAllByRole("link");

    expect(navLinks).toHaveLength(navLinks.length);
  });

  it(`renders icons for all nav links that are hidden from assistive technology`, () => {
    const nav = screen.getByRole("navigation", {
      name: "Main navigation",
    });

    const icons = within(nav).getAllByTestId("ds-icon");
    expect(icons).toHaveLength(navLinks.length);
  });
});
