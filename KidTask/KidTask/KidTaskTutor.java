import java.util.Scanner;

public class KidTaskTutor {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to KidTask AI Tutor");
        System.out.println("Type 'exit' to quit.");

        while (true) {
            System.out.print("\nAsk a question: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }

            try {
                String response = processQuestion(input);
                System.out.println("Tutor: " + response);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }

    public static String processQuestion(String question) {
        if (question == null || question.isEmpty()) {
            throw new IllegalArgumentException("Input cannot be empty.");
        }

        if (question.length() < 5) {
            throw new IllegalArgumentException("Question is too short. Please provide more details.");
        }

        if (!isRelatedToKidTask(question)) {
            return "This question is outside the scope of KidTask. Please ask about task management.";
        }

        if (question.toLowerCase().contains("add task")) {
            return "To add a task, go to the task creation screen and enter the task details.";
        }

        if (question.toLowerCase().contains("delete task")) {
            return "You can delete a task by selecting it and clicking the delete button.";
        }

        return "Your question is valid, but more details are needed to provide a precise answer.";
    }

    private static boolean isRelatedToKidTask(String question) {
        String q = question.toLowerCase();
        return q.contains("task") || q.contains("kidtask");
    }
}


