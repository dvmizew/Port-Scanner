# Network Scanner

## Introduction
Welcome to the Port Scanner repository! This project provides a simple tool that allows you to scan the IP or the hostname for open ports.

## Features
- Scan open ports on a single host or a range of hosts.
- Specify custom port ranges for scanning.
- Multi-threaded scanning for faster results.
- Cancel ongoing scans at any time.
- Generate a report of scan results.

## Installation
To use the Network Scanner, follow these steps:
1. Clone the repository:
    git clone https://github.com/dvmizew/Port-Scanner.git

2. Navigate to the project directory:
   cd Port-Scanner

3. Compile the Java files:
   javac *.java

4. Run the application:
   java Main

## Usage
- Upon running the application, the GUI will appear.
- Enter the target host (IP address or hostname), start port, and end port in the respective fields.
- Click on the "Scan Network" button to initiate the scan.
- The scan progress will be displayed in the text area along with open ports found.
- Click on the "Cancel" button to stop an ongoing scan.
- After the scan is complete, you can generate a report by clicking on the "Generate Report" button.

## Example
Here's a simple example of using the Network Scanner GUI:

1. Enter the target host as "192.168.0.1".
2. Set the start port to "1" and the end port to "1024".
3. Click on the "Scan Network" button.
4. Wait for the scan to complete, and view the results in the text area.
5. Optionally, generate a report by clicking on the "Generate Report" button.

## Contributing
Contributions are welcome! If you have any ideas for improvements, new features, or bug fixes, feel free to open an issue or submit a pull request.
