import java.util.Scanner;

// Initial version of this class was generated with AI assistance (DeepSeek).
// Human-in-the-loop revisions were applied to improve input validation
// and to make responses more child-friendly.

public class KidTaskTutor {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to KidTask AI Tutor 🎉");
        System.out.println("Type 'exit' to quit.");

        while (true) {
            System.out.print("\nAsk a question: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye! 👋 See you next time!");
                break;
            }

            try {
                String response = processQuestion(input);
                System.out.println("Tutor: " + response);
            } catch (IllegalArgumentException e) {
                System.out.println("Oops! " + e.getMessage());
            }
        }
        scanner.close();
    }

    public static String processQuestion(String question) {

        if (question == null || question.isEmpty()) {
            throw new IllegalArgumentException(
                "I didn’t catch that 🤔 Can you type something so I can help?"
            );
        }

        if (question.length() < 5) {
            throw new IllegalArgumentException(
                "Can you tell me a bit more? I want to help you! 🌟"
            );
        }

        if (!isRelatedToKidTask(question)) {
            return "Oops! 😅 I can help with tasks, homework, or chores. Let’s talk about those!";
        }

        if (question.toLowerCase().contains("homework")) {
            return "Homework time! 📚 Let’s do it step by step. You’ve got this! 💪";
        }

        if (question.toLowerCase().contains("add task")) {
            return "Great! 📝 Let’s add a new task together. Just tell me what you need to do 😊";
        }

        if (question.toLowerCase().contains("delete task")) {
            return "No problem! ❌ You can delete a task by choosing it and tapping delete.";
        }

        return "Nice question! 😊 Can you give me a little more detail so I can help better?";
    }

    private static boolean isRelatedToKidTask(String question) {
        String q = question.toLowerCase();
        return q.contains("task") || q.contains("kidtask") || q.contains("homework");
    }
}
