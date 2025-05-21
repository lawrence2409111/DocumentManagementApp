# DocumentManagementApp
Deployment prerequisites to Deploy on AWS EC2 Services
First we need AWS Acoount
AWS CLI Configured: Ensure you have the AWS CLI installed and configured with appropriate IAM user credentials that have permissions to create EC2 instances, security groups, and other necessary AWS resources.
Terraform Installed: Download and install Terraform on your local machine.
SSH Key Pair: Create an SSH key pair in your AWS account (e.g., via the EC2 console) or generate one locally and upload the public key to AWS. This key will be used to securely connect to your EC2 instance.
Java Application (JAR/WAR): Have your compiled Java application (e.g., a .jar for a Spring Boot app or a .war for a traditional web app like Tomcat) ready for deployment. DocumentManagment APP is Complied as .jar File
Deployment Steps below:
Create a dedicated directory for your Terraform project. A typical structure might look like this:
terraform-java-app/
├── main.tf
├── variables.tf
├── outputs.tf
├── user-data.sh
└── java-app/
    └── your-java-app.jar 
In main.tf need to do Terraform Configuration
variables.tf need to Define Variables
Define outputs.tf
user-data.sh (Shell Script for Instance Configuration)
Navigate to terraform project
Run terraform init , terraform plan , terraform apply
