>< **Note:**  
> The BeePlan implementation code was handled by a teammate as part of the group work.  
> My responsibility in this project was limited to the AI Tutor evaluation and comparison tasks.

# AI Tutor – BeePlan (OpenAI)

## Tool Description
BeePlan uses an OpenAI-based AI Tutor to assist users with task planning and organization.  
The tutor is designed to provide explanatory, user-friendly responses rather than technical instructions.



## Example Questions and Responses

### Q1: Valid Question
**Question:** How can I organize my daily tasks more efficiently?  

**Response:**  
BeePlan suggests categorizing tasks based on priority, setting realistic deadlines, and breaking large tasks into smaller subtasks to improve productivity.



### Q2: Ambiguous Question
**Question:** Task help  

**Response:**  
BeePlan requests clarification from the user and provides examples of task-related questions to guide the user toward a clearer input.



### Q3: Out-of-Scope Question
**Question:** How do I cook pasta?  

**Response:**  
BeePlan indicates that the question is outside the scope of task management and redirects the user back to task-related topics.



## Error Handling Behavior
BeePlan handles incorrect or unclear inputs by:
- Asking for clarification when the question is ambiguous
- Politely rejecting out-of-scope questions
- Providing guidance instead of technical error messages



## Evaluation

### Output Quality
BeePlan produces detailed and easy-to-understand responses, making it suitable for non-technical users.

### Usability
The tutor is user-friendly and conversational, which improves the overall user experience.

### Output Trustworthiness
Responses are consistent and logically structured, increasing user confidence in the system.



## Summary
BeePlan AI Tutor focuses on clarity and user guidance. While it does not provide highly technical answers, it is effective for users seeking help with task planning and organization.
