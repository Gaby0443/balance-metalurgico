# Metallurgical Mass-Balance Analysis

Independent Java prototype developed to study and reproduce a metallurgical mass-balance calculation from a reference case.

The application processes granulometric measurements, estimates normalized stream tonnages, calculates weighting factors, adjusts measured values subject to mass-balance constraints, validates the resulting residuals, and exports the results as CSV files.

> **Project status:** Experimental engineering-computation prototype.  
> The implementation reproduces the expected results for the reference dataset included in this repository. Broader validation with additional datasets is still required.

---

## Overview

Metallurgical measurements are subject to experimental error and may not satisfy material-balance equations exactly.

This project explores a computational approach for reconciling those measurements by:

1. Reading granulometric data from CSV.
2. Calculating weighting factors for measured values.
3. Estimating normalized stream tonnages.
4. Applying constrained data adjustment.
5. Solving the adjustment system using Lagrange multipliers.
6. Calculating relative errors and balance residuals.
7. Validating the resulting mass balance.
8. Exporting the calculated values for further analysis.

The project was developed primarily as a learning exercise in applying software engineering and numerical methods to an engineering problem.

---

## Technologies

- **Java 21**
- **Maven**
- CSV data processing
- Object-Oriented Programming
- Numerical methods
- Matrix-based calculations
- Lagrange multipliers

No external Java libraries are currently required.

---

## Calculation Workflow

```text
Granulometric CSV data
        │
        ▼
Input validation
        │
        ▼
Weighting-factor calculation
        │
        ▼
Normalized tonnage estimation
        │
        ▼
Constrained data adjustment
        │
        ▼
Lagrange multiplier system
        │
        ▼
Adjusted values
        │
        ▼
Residual & relative-error validation
        │
        ▼
CSV result export
