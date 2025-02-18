import { cleanup, screen, within, render } from "@testing-library/react";
import { createMockTopic } from "src/domain/topic/topic-test-helper";
import TopicList from "src/app/features/browse-topics/components/topic-list/TopicList";

const mockedTopicNames = ["Name one", "Name two", "Name three", "Name four"];
const mockedTopics = mockedTopicNames.map((name, index) =>
  createMockTopic({ topicName: name, topicId: index })
);

describe("TopicList.tsx", () => {
  describe("shows all topics as a list", () => {
    beforeAll(() => {
      render(<TopicList topics={mockedTopics} />);
    });

    afterAll(cleanup);

    it("renders a topic list", async () => {
      const list = screen.getByRole("list", { name: "Topics" });

      expect(list).toBeVisible();
    });

    it("shows each topic as list item", async () => {
      const list = screen.getByRole("list", { name: "Topics" });

      mockedTopics.forEach((topic) => {
        const topicCard = within(list).getByText(topic.topicName);

        expect(topicCard).toBeVisible();
      });
    });
  });
});
