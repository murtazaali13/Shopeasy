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
## Day 7: Add Database Products to the Shopping Cart

**Date:** 23 November 2026

### Objective

Allow products loaded from MySQL to be added to the existing shopping cart.

### Work Completed

1. Added an “Add to cart” button to each database product card.
2. Connected the buttons to the existing cart functionality.
3. Included each product’s database ID, name, and price so the cart can display it.
4. Rebuilt and redeployed the application to Tomcat.
5. Confirmed in the browser that a database product can be added to the cart.

### Outcome

Products loaded from MySQL can now be added to the website’s client-side shopping cart.

### Scope

The cart still runs in the browser. Cart saving between visits, checkout, and order storage have not been implemented.

### Next Steps

- Plan the next shopping cart or checkout feature.
## Day 8: Search, Filter, and Display Database Products

**Date:** 24 November 2026

### Objective

Make products loaded from MySQL work with the existing search and category filters, and display matching product pictures.

### Work Completed

1. Added category, name, and search information to database product cards.
2. Updated the filters to include products loaded from MySQL.
3. Applied the current filters after database products finish loading.
4. Assigned product pictures to the four sample database products.
5. Adjusted picture display so the full product images fit inside the cards.
6. Checked the Daily Sip Bottle search, Wellness filter, and product pictures.

### Outcome

Products loaded from MySQL can be searched and filtered by category, and their pictures display correctly.

### Next Steps

- Begin planning user registration and login.