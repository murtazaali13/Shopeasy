# ShopEasy — Day-wise Development Log

## Day 1: Project Planning and Repository Setup

**Project Name:** ShopEasy — Online Shopping Website

**Date:** 10 October 2026

### Objective
To plan the ShopEasy website, identify its main features, select the proposed technologies, and set up version control.

### Work Completed
1. Created the ShopEasy project folder in VS Code.
2. Created the `README.md` file containing the project introduction, objectives, and features.
3. Created a GitHub repository named `ShopEasy`.
4. Pushed the initial project documentation to GitHub.

### Tools Used
- Visual Studio Code
- Git
- GitHub
- Markdown

### Planned Technologies
- HTML
- CSS
- JavaScript
- Java
- MySQL
- Apache Tomcat

### Outcome
The initial project structure and documentation have been created and uploaded to GitHub.

### Next Steps
- Create the project documentation folder and Git ignore file.
- Begin developing the website homepage.
- Track future development work through Git commits and GitHub.

.
## Day 2: Responsive Homepage and Product Browsing

**Date:** 11 October 2026

### Objective

Build the first frontend version of the ShopEasy homepage.

### Work Completed

1. Created the homepage using HTML.
2. Added responsive styling for desktop and mobile screens.
3. Added sample products with category filters and search.
4. Added a client-side cart count that updates when products are added.

### Tools Used

- Visual Studio Code
- HTML
- CSS
- JavaScript
- Git and GitHub

### Outcome

The static homepage displays sample products. Visitors can search and filter products, and the cart count increases when they click “Add to cart.”

### Scope

This is a frontend demonstration with sample products. A backend, user accounts, payment, and checkout have not been implemented.

### Next Steps

- Review the homepage and make any design changes.
- Plan the next project feature.

## Day 3: ShopEasy homepage redesign

- Updated the homepage colors and styling to give it a brighter, more eye-catching look.
- Improved the product card layout and spacing for desktop and smaller screens.
- Kept the existing search, category filters, and add-to-cart counter.
- The page uses sample products and runs in the browser. Backend, login, and checkout features are not implemented.
# Day 5: Java Backend and MySQL Product API

**Project:** ShopEasy — Online Shopping Website
**Date:** 22 November 2026

## Objective

Create a Java backend endpoint that reads ShopEasy products from MySQL.

## Work Completed

1. Created a Java web application using Maven and Jakarta Servlets.
2. Connected Java to MySQL using MySQL Connector/J.
3. Created a `GET /api/products` endpoint.
4. Deployed the application to Apache Tomcat.
5. Opened the API in a browser and confirmed it returned four products as JSON.

## Outcome

The ShopEasy backend can read product information from the MySQL database and return it through the API.

## Next Steps

Connect the homepage to the products API so its product cards use the database data.
## Day 6: Display Products from MySQL

**Project:** ShopEasy — Online Shopping Website  
**Date:** 22 November 2026

### Objective

Show product information from the MySQL database on the ShopEasy website.

### Work Completed

1. Placed copies of the homepage files in the Java web application so Tomcat serves the page and API.
2. Added a “Products from the database” section to the homepage.
3. Used JavaScript to request products from `/api/products`.
4. Displayed the product name, category, description, price, and stock quantity returned by the API.

### Outcome

The ShopEasy page displays the four products returned by the Java backend from MySQL.

### Next Steps

Replace the original sample catalog with database products and connect those products to the cart.