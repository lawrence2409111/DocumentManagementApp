# DocumentManagementApp
1. Deployment prerequisites to Deploy on AWS EC2 Services
2. First we need AWS Acoount
3. AWS CLI Configured: Ensure you have the AWS CLI installed and configured with appropriate IAM user credentials that have permissions to create EC2 instances, 
   security groups, and other necessary AWS resources.
4. Terraform Installed: Download and install Terraform on your local machine.
5. SSH Key Pair: Create an SSH key pair in your AWS account (e.g., via the EC2 console) or generate one locally and upload the public key to AWS. This key will be 
   used to securely connect to your EC2 instance.
6. Java Application (JAR/WAR): Have your compiled Java application (e.g., a .jar for a Spring Boot app or a .war for a traditional web app like Tomcat) ready for 
   deployment. DocumentManagment APP is Complied as .jar File
7. Deployment Steps below:
8. Create a dedicated directory for your Terraform project. A typical structure might look like this:
   terraform-java-app/
   ├── main.tf
   ├── variables.tf
   ├── outputs.tf
   ├── user-data.sh
   └── java-app/
    └── Document-Management-app.jar 
9. In main.tf need to do Terraform Configuration
10. variables.tf need to Define Variables
11. Define outputs.tf
12. user-data.sh (Shell Script for Instance Configuration)
13. Navigate to terraform project
14. Run terraform init , terraform plan , terraform apply
