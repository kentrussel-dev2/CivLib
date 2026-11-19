/* CivLib — Civil Library iOS & Android PWA Application Engine */
(function () {
  'use strict';

  // --- 1. ENGINEERING SUBJECTS (All 12 Civil, Allied, Math/Physics & Masters Tracks) ---
  const SUBJECTS = {
    MATH_PHYSICS: { key: 'MATH_PHYSICS', name: 'Engineering Math, Calculus & Physics', short: 'Math & Physics', code: 'ENGR 101', year: 1, order: 1, color: '#395B64' },
    MECHANICS: { key: 'MECHANICS', name: 'Engineering Mechanics (Statics & Dynamics)', short: 'Mechanics', code: 'CE 101', year: 1, order: 2, color: '#7D3C27' },
    SURVEYING: { key: 'SURVEYING', name: 'Geomatics & Elementary Surveying', short: 'Surveying', code: 'CE 112', year: 1, order: 3, color: '#2E6153' },
    ALLIED_ENGINEERING: { key: 'ALLIED_ENGINEERING', name: 'Mechanical, Electrical & Allied Engineering', short: 'Allied Eng.', code: 'ENGR 210', year: 2, order: 4, color: '#6E473B' },
    MATERIALS: { key: 'MATERIALS', name: 'Strength of Materials & Construction Materials', short: 'Materials', code: 'CE 205', year: 2, order: 5, color: '#8A5A2B' },
    HYDRAULICS: { key: 'HYDRAULICS', name: 'Fluid Mechanics, Hydraulics & Hydrology', short: 'Hydraulics', code: 'CE 220', year: 2, order: 6, color: '#1F5673' },
    STRUCTURAL: { key: 'STRUCTURAL', name: 'Structural Theory, Steel & Reinforced Concrete', short: 'Structural', code: 'CE 301', year: 3, order: 7, color: '#843428' },
    GEOTECHNICAL: { key: 'GEOTECHNICAL', name: 'Soil Mechanics & Foundation Engineering', short: 'Geotechnical', code: 'CE 315', year: 3, order: 8, color: '#694F35' },
    TRANSPORTATION: { key: 'TRANSPORTATION', name: 'Highway, Traffic & Transportation Engineering', short: 'Transportation', code: 'CE 330', year: 3, order: 9, color: '#3A5342' },
    ENVIRONMENTAL: { key: 'ENVIRONMENTAL', name: 'Water Supply, Sanitary & Environmental Eng.', short: 'Environmental', code: 'CE 402', year: 4, order: 10, color: '#2C635A' },
    CONSTRUCTION: { key: 'CONSTRUCTION', name: 'Construction Management, Estimating & Contracts', short: 'Construction', code: 'CE 408', year: 4, order: 11, color: '#73532B' },
    MASTERS_GRADUATE: { key: 'MASTERS_GRADUATE', name: 'Masters, Seismic, FEM & Bridge Engineering', short: 'Masters / MSCE', code: 'MSCE 501', year: 5, order: 12, color: '#5D467E' }
  };

  const COURSE_PROGRAMS = [
    'BS Civil Engineering (BSCE)',
    'MS / Masters in Civil & Structural Eng. (MSCE)',
    'Mechanical, Electrical & Allied Engineering',
    'Architecture & Construction Management',
    'Engineering Math, Physics & General STEM'
  ];

  const STUDY_GOALS = [
    { title: 'Current Semester Courses', badge: 'Semester Track', sub: 'Prioritize textbooks matching your current year & semester subjects' },
    { title: 'CE Licensure & Board Exam Review', badge: 'Board Exam', sub: 'Focus on Math, Hydraulics, Structural Design & Geotechnical board topics' },
    { title: 'Structural & Foundation Design Mastery', badge: 'Design Track', sub: 'Deep-dive into Reinforced Concrete, Steel, Soil Mechanics & Foundations' },
    { title: 'Masters, Research, FEM & Earthquake Eng.', badge: 'Masters / Grad', sub: 'Advanced finite elements, structural dynamics, plates & shells, and seismic design' },
    { title: 'Engineering Math, Physics & Allied Foundations', badge: 'STEM & Allied', sub: 'Calculus, differential equations, engineering physics, thermodynamics & circuits' },
    { title: 'Capstone, Field Practice & Estimating', badge: 'Field & Project', sub: 'Construction management, highway design, surveying & environmental systems' }
  ];

  // --- 2. CURATED ENGINEERING TEXTBOOK CATALOG WITH EMBEDDED STUDY CHAPTERS & PDF LINKS ---
  const STARTER_BOOKS = [
    {
      id: 'calc_piskunov', title: 'Differential and Integral Calculus (Vol. 1 & 2)', author: 'N. Piskunov',
      subjectKey: 'MATH_PHYSICS', courseCode: 'ENGR 101', year: 1969, totalPages: 895, isStarter: true,
      coverUrl: './icons/cover-structural.jpg',
      pdfUrl: 'https://archive.org/download/differential-and-integral-calculus-n.-piskunov/Differential%20and%20Integral%20Calculus%20-%20N.%20Piskunov.pdf',
      description: 'Complete engineering calculus monograph covering limits, derivatives, definite integrals, partial derivatives, multiple integrals, Fourier series, and differential equations.',
      chapters: [
        { page: 1, title: 'Cover & Title Monograph — Differential & Integral Calculus', body: 'DIFFERENTIAL AND INTEGRAL CALCULUS\nBy N. Piskunov (Mir Publishers)\n\nComprehensive engineering mathematics reference for BSCE, Mechanical, Electrical, and STEM students.\n\nCore Topics Indexed:\n• Ch. 1–4: Limits, Continuity & Differentiation of Algebraic/Trigonometric Functions\n• Ch. 5–7: Maxima, Minima, Curvature & Taylor Series\n• Ch. 10–12: Indefinite and Definite Integrals in Engineering Mechanics\n• Ch. 13–16: Differential Equations of Beam Deflection & Harmonic Vibration' },
        { page: 14, title: 'Chapter 1: Number, Variable, Function & Limits', body: '1.1 Engineering Functions & Limits\nA variable y is a function of x if to every value of x in domain D there corresponds a definite value of y = f(x).\n\nFundamental Limit Theorems:\n• lim (x→0) [sin(x) / x] = 1\n• lim (n→∞) (1 + 1/n)^n = e ≈ 2.7182818\n\nApplication in Structural & Fluid Analysis:\nInfinitesimal stress elements dx, dy, dz rely on continuous limit behavior across cross-sections.' },
        { page: 48, title: 'Chapter 3: Derivative and Differential in Mechanics', body: '3.1 Physical Meaning of the Derivative\n• Instantaneous Velocity: v(t) = ds/dt\n• Instantaneous Acceleration: a(t) = dv/dt = d²s/dt²\n• Bending Slope of Elastic Beam: θ(x) = dy/dx, Curvature κ ≈ d²y/dx² = M(x) / (E·I)\n\nDifferentiation Formulas:\n• d/dx (u·v) = u(dv/dx) + v(du/dx)\n• d/dx [ln(u)] = (1/u)(du/dx)\n• d/dx [sin(u)] = cos(u)(du/dx)' },
        { page: 112, title: 'Chapter 10: Indefinite & Definite Integrals for Centroids & Moments of Inertia', body: '10.1 Engineering Section Properties via Integration\n• First Moment of Area: Qx = ∫ y dA , Qy = ∫ x dA\n• Centroid Coordinates: x̄ = (∫ x dA) / A , ȳ = (∫ y dA) / A\n• Area Moment of Inertia: Ix = ∫ y² dA , Iy = ∫ x² dA\n• Parallel-Axis Theorem: I = I_centroid + A·d²\n• Rectangular Beam Cross-Section (width b, depth h): I_centroid = b·h³ / 12' },
        { page: 210, title: 'Chapter 16: Ordinary Differential Equations in Engineering', body: '16.1 Second-Order Linear Differential Equations\nGeneral Form: m(d²u/dt²) + c(du/dt) + k·u = F(t)\n• Natural Circular Frequency: ω_n = √(k / m)\n• Critical Damping: c_cr = 2·m·ω_n\n• Euler Column Buckling ODE: E·I (d²y/dx²) + P·y = 0 → P_cr = π²·E·I / (K·L)²' }
      ]
    },
    {
      id: 'physics_irodov', title: 'Fundamental Laws of Mechanics & Engineering Physics', author: 'I. E. Irodov',
      subjectKey: 'MATH_PHYSICS', courseCode: 'ENGR 102', year: 1980, totalPages: 294, isStarter: true,
      coverUrl: './icons/cover-hydraulics.jpg',
      pdfUrl: 'https://archive.org/download/IrodovFundamentalLawsOfMechanics/Irodov-Fundamental-Laws-of-Mechanics.pdf',
      description: 'Classical and engineering physics monograph covering kinematics, Newton laws, work-energy theorem, impulse-momentum, rigid body rotation, and wave oscillations.',
      chapters: [
        { page: 1, title: 'Cover & Title Monograph — Fundamental Laws of Mechanics', body: 'FUNDAMENTAL LAWS OF MECHANICS\nBy I. E. Irodov\n\nFoundational physics text for first-year engineering mechanics, kinematics, conservation of energy, angular momentum, and harmonic oscillations.' },
        { page: 22, title: 'Chapter 2: Conservation of Energy & Momentum', body: '2.1 Work-Energy Theorem\nW = ∫ F · dr = ΔK = ½ m(v₂² - v₁²)\n\nConservation of Mechanical Energy:\nK₁ + U₁ + W_nonconservative = K₂ + U₂\nGravitational Potential Energy: U_g = m·g·h\nElastic Strain Energy in Spring/Bar: U_e = ½ k·x² = P²·L / (2·A·E)' },
        { page: 68, title: 'Chapter 5: Dynamics of a Solid / Rigid Body', body: '5.1 Rotational Equation of Motion\nΣM_z = I_z · α\nwhere I_z = ∫ r² dm is the mass moment of inertia and α = dω/dt is angular acceleration.\nRotational Kinetic Energy: K_rot = ½ I_z · ω²' }
      ]
    },
    {
      id: 'mech_timoshenko', title: 'Engineering Mechanics: Statics and Dynamics', author: 'S. Timoshenko & D. H. Young',
      subjectKey: 'MECHANICS', courseCode: 'CE 101', year: 1956, totalPages: 548, isStarter: true,
      coverUrl: './icons/cover-structural.jpg',
      pdfUrl: 'https://archive.org/download/in.ernet.dli.2015.205456/2015.205456.Engineering-Mechanics.pdf',
      description: 'Classic civil engineering textbook on concurrent/coplanar force systems, truss analysis, friction, centroids, moments of inertia, kinematics, and D’Alembert’s principle.',
      chapters: [
        { page: 1, title: 'Cover & Title Monograph — Engineering Mechanics (Timoshenko & Young)', body: 'ENGINEERING MECHANICS: STATICS AND DYNAMICS\nBy Stephen Timoshenko & D. H. Young\n\nStandard CE 101 reference on static equilibrium, trusses, cables, friction, centroids, and rigid-body dynamics.' },
        { page: 18, title: 'Chapter 1: Principles of Statics & Concurrent Forces', body: '1.1 Equations of Static Equilibrium (2D Coplanar System)\nFor a rigid body to remain in static equilibrium:\n• ΣFx = 0  (Horizontal force balance)\n• ΣFy = 0  (Vertical force balance)\n• ΣMz = 0  (Moment balance about any point O)\n\nResultant of Two Forces P and Q with angle α:\nR = √(P² + Q² + 2·P·Q·cos α)' },
        { page: 50, title: 'Chapter 3: Method of Joints & Method of Sections for Plane Trusses', body: '3.1 Determinacy & Stability of Plane Trusses\nm + r = 2j\nwhere m = number of members, r = reaction components (usually 3), j = number of joints.\n\n• Method of Joints: Isolate a pin joint with ≤ 2 unknown member forces; apply ΣFx = 0 and ΣFy = 0.\n• Method of Sections (Ritter’s Method): Cut through ≤ 3 members and take moments ΣM = 0 about the intersection of two members to solve the third directly.' },
        { page: 118, title: 'Chapter 5: Centroids and Moments of Inertia of Plane Areas', body: '5.1 Composite Section Procedure\n1. Divide structural shape (I-beam, T-beam, channel) into simple rectangles/triangles.\n2. Compute ȳ = (Σ A_i · y_i) / (Σ A_i).\n3. Apply Parallel-Axis Theorem: I_x = Σ (I_ci + A_i · d_i²).' }
      ]
    },
    {
      id: 'survey_breed', title: 'Principles and Practice of Surveying (Elementary & Higher)', author: 'Charles B. Breed & George L. Hosmer',
      subjectKey: 'SURVEYING', courseCode: 'CE 112', year: 1945, totalPages: 717, isStarter: true,
      coverUrl: './icons/cover-geotech.jpg',
      pdfUrl: 'https://archive.org/download/principlespracti01breeuoft/principlespracti01breeuoft.pdf',
      description: 'Comprehensive geomatics manual covering differential leveling, transit traverse computations, latitude and departure, Bowditch compass rule, stadia, and horizontal/vertical highway curves.',
      chapters: [
        { page: 1, title: 'Cover & Title Monograph — Principles and Practice of Surveying', body: 'PRINCIPLES AND PRACTICE OF SURVEYING\nBy Charles B. Breed & George L. Hosmer (MIT)\n\nComplete CE 112 textbook on leveling, compass & transit traverses, earthwork volumes, and route curves.' },
        { page: 42, title: 'Chapter 4: Differential & Profile Leveling', body: '4.1 Leveling Equations\n• Height of Instrument: HI = Elev_BM + BS (Backsight)\n• Elevation of Turning Point: Elev_TP = HI - FS (Foresight)\n• Arithmetic Check: ΣBS - ΣFS = Elev_last - Elev_first\n• Combined Curvature & Refraction Correction: h_cr ≈ 0.0675 · K² (meters, with K in km)' },
        { page: 135, title: 'Chapter 8: Latitudes, Departures & Compass (Bowditch) Rule', body: '8.1 Traverse Computations\nFor a line of length D and azimuth α:\n• Latitude (ΔN) = D · cos(α)\n• Departure (ΔE) = D · sin(α)\n• Linear Misclosure: e = √[(ΣLat)² + (ΣDep)²]\n• Relative Precision = 1 : (Perimeter / e)\n\nBowditch (Compass) Rule Correction:\nC_lat = - (ΣLat) · (d / ΣD)  |  C_dep = - (ΣDep) · (d / ΣD)' }
      ]
    },
    {
      id: 'allied_elec_kasatkin', title: 'Basic Electrical Engineering & Circuits', author: 'A. S. Kasatkin & M. V. Nemtsov',
      subjectKey: 'ALLIED_ENGINEERING', courseCode: 'ENGR 210', year: 1986, totalPages: 504, isStarter: true,
      coverUrl: './icons/cover-hydraulics.jpg',
      pdfUrl: 'https://archive.org/download/BasicElectricalEngineeringKasatkin/Basic-Electrical-Engineering-Kasatkin.pdf',
      description: 'Allied engineering textbook on DC/AC circuit analysis, Kirchhoff laws, three-phase power systems, transformers, induction motors, and building electrical distribution.',
      chapters: [
        { page: 1, title: 'Cover & Title Monograph — Basic Electrical Engineering', body: 'BASIC ELECTRICAL ENGINEERING\nBy A. S. Kasatkin & M. V. Nemtsov\n\nAllied Engineering reference on DC/AC circuits, three-phase power, transformers, and electrical machinery.' },
        { page: 25, title: 'Chapter 1: Ohm’s Law, Kirchhoff’s Laws & Power', body: '1.1 Fundamental Circuit Laws\n• Ohm’s Law: V = I · R\n• Kirchhoff’s Current Law (KCL): ΣI_in = ΣI_out at any node\n• Kirchhoff’s Voltage Law (KVL): ΣV = 0 around any closed loop\n• Three-Phase Real Power: P = √3 · V_L · I_L · cos(φ)' }
      ]
    },
    {
      id: 'allied_thermo_kirillin', title: 'Engineering Thermodynamics & Heat Transfer', author: 'V. A. Kirillin, V. V. Sychev & A. E. Sheindlin',
      subjectKey: 'ALLIED_ENGINEERING', courseCode: 'ENGR 215', year: 1981, totalPages: 560, isStarter: true,
      coverUrl: './icons/cover-structural.jpg',
      pdfUrl: 'https://archive.org/download/EngineeringThermodynamicsKirillin/Engineering-Thermodynamics-Kirillin.pdf',
      description: 'Foundational mechanical & allied engineering monograph on the First and Second Laws of Thermodynamics, entropy, Carnot/Rankine/Otto cycles, and heat conduction.',
      chapters: [
        { page: 1, title: 'Cover & Title Monograph — Engineering Thermodynamics', body: 'ENGINEERING THERMODYNAMICS\nBy V. A. Kirillin, V. V. Sychev & A. E. Sheindlin\n\nCovers thermodynamic state equations, First & Second Laws, gas/vapor power cycles, and heat transfer.' },
        { page: 34, title: 'Chapter 2: First and Second Laws of Thermodynamics', body: '2.1 Thermodynamic Laws\n• Ideal Gas Law: P·V = m·R·T\n• First Law (Closed System): Q - W = ΔU\n• First Law (Steady-Flow Control Volume): q - w_s = Δh + ½(V₂² - V₁²) + g(z₂ - z₁)\n• Carnot Efficiency: η_max = 1 - (T_cold / T_hot)' }
      ]
    },
    {
      id: 'strength_timoshenko', title: 'Strength of Materials (Part I: Elementary Theory & Problems)', author: 'Stephen P. Timoshenko',
      subjectKey: 'MATERIALS', courseCode: 'CE 205', year: 1955, totalPages: 454, isStarter: true,
      coverUrl: './icons/cover-structural.jpg',
      pdfUrl: 'https://archive.org/download/StrengthOfMaterialsPart1/Strength%20of%20Materials%20Part%201.pdf',
      description: 'Definitive mechanics of materials text on axial tension/compression, Hooke’s law, shear and bending moment diagrams, flexure formula, Mohr’s circle, torsion, and elastic beam deflection.',
      chapters: [
        { page: 1, title: 'Cover & Title Monograph — Strength of Materials (Timoshenko)', body: 'STRENGTH OF MATERIALS — PART I\nBy Stephen P. Timoshenko\n\nEssential CE 205 textbook on stress-strain analysis, shear & moment diagrams, beam flexure, torsion, and column buckling.' },
        { page: 16, title: 'Chapter 1: Tension and Compression within the Elastic Limit', body: '1.1 Hooke’s Law & Axial Deformation\n• Normal Stress: σ = P / A\n• Normal Strain: ε = δ / L\n• Hooke’s Law: σ = E · ε  →  Axial Elongation: δ = (P · L) / (A · E)\n• Thermal Strain: ε_T = α · ΔT  |  Poisson’s Ratio: ν = - (ε_lateral / ε_axial)' },
        { page: 50, title: 'Chapter 3 & 4: Shear Force, Bending Moment & Flexure Formula', body: '4.1 Differential Relationships in Beams\n• dV/dx = -w(x)   (Slope of Shear Diagram equals negative load intensity)\n• dM/dx = V(x)    (Slope of Moment Diagram equals Shear Force; Maximum Moment occurs where V = 0)\n\nFlexure Formula (Bending Stress):\nσ_b = (M · y) / I  |  σ_max = M / S  (where Section Modulus S = I / c)\n\nTransverse Shear Stress Formula:\nτ = (V · Q) / (I · b)' },
        { page: 140, title: 'Chapter 6 & 8: Beam Deflections & Euler Column Buckling', body: '6.1 Standard Elastic Beam Deflections\n• Simply Supported Beam with Uniform Load w: Δ_max = (5 · w · L⁴) / (384 · E · I) , M_max = w·L² / 8\n• Simply Supported Beam with Center Load P: Δ_max = (P · L³) / (48 · E · I) , M_max = P·L / 4\n• Cantilever Beam with End Load P: Δ_max = (P · L³) / (3 · E · I) , M_max = P·L\n\n8.1 Euler Critical Buckling Load:\nP_cr = (π² · E · I) / (K · L)²' }
      ]
    },
    {
      id: 'hydraulics_king', title: 'Handbook of Hydraulics & Fluid Mechanics', author: 'Horace Williams King',
      subjectKey: 'HYDRAULICS', courseCode: 'CE 220', year: 1954, totalPages: 576, isStarter: true,
      coverUrl: './icons/cover-hydraulics.jpg',
      pdfUrl: 'https://archive.org/download/in.ernet.dli.2015.165031/2015.165031.Handbook-Of-Hydraulics.pdf',
      description: 'Comprehensive civil hydraulics reference on hydrostatic pressure, buoyancy, Bernoulli energy equation, Darcy-Weisbach pipe friction, sharp-crested weirs, and Manning open-channel flow.',
      chapters: [
        { page: 1, title: 'Cover & Title Monograph — Handbook of Hydraulics (H. W. King)', body: 'HANDBOOK OF HYDRAULICS\nBy Horace Williams King\n\nCore CE 220 reference for hydrostatics, orifices, weirs, pipe networks, and open-channel hydraulics.' },
        { page: 20, title: 'Chapter 1: Hydrostatic Pressure & Submerged Surfaces', body: '1.1 Hydrostatics\n• Pressure at depth h: p = γ · h = ρ · g · h  (γ_water = 9.81 kN/m³)\n• Total Hydrostatic Force on Plane Surface: F = γ · h_c · A\n• Depth to Center of Pressure: y_cp = y_c + (I_cg / (y_c · A))\n• Archimedes Buoyancy Force: F_B = γ_fluid · V_displaced' },
        { page: 50, title: 'Chapter 4 & 6: Bernoulli Equation, Darcy-Weisbach & Manning Open-Channel Flow', body: '4.1 Continuity & Bernoulli Energy Equation\n• Continuity: Q = A₁·V₁ = A₂·V₂\n• Bernoulli: (p₁/γ) + (V₁²/2g) + z₁ + h_pump = (p₂/γ) + (V₂²/2g) + z₂ + h_f\n• Darcy-Weisbach Pipe Head Loss: h_f = f · (L / D) · (V² / 2g)\n\n6.1 Manning’s Open-Channel Formula:\nV = (1 / n) · R^(2/3) · S^(1/2)   and   Q = A · V\nwhere Hydraulic Radius R = A / P (Area divided by Wetted Perimeter).' }
      ]
    },
    {
      id: 'struct_hool', title: 'Reinforced Concrete & Structural Theory Monograph', author: 'George A. Hool & Nathan C. Johnson',
      subjectKey: 'STRUCTURAL', courseCode: 'CE 301', year: 1952, totalPages: 696, isStarter: true,
      coverUrl: './icons/cover-structural.jpg',
      pdfUrl: 'https://archive.org/download/concreteengineer00hoolrich/concreteengineer00hoolrich.pdf',
      description: 'Structural analysis and reinforced concrete design handbook covering influence lines, portal/cantilever frame methods, moment distribution (Hardy Cross), flexural beam design, and shear stirrups.',
      chapters: [
        { page: 1, title: 'Cover & Title Monograph — Structural Theory & Reinforced Concrete', body: 'REINFORCED CONCRETE & STRUCTURAL DESIGN\nBy George A. Hool & Nathan C. Johnson\n\nComprehensive CE 301 volume on indeterminate structural analysis, Hardy Cross moment distribution, and RC beam/column design.' },
        { page: 50, title: 'Chapter 5: Reinforced Concrete Flexural Design & Whitney Stress Block', body: '5.1 Ultimate Strength (LRFD) Flexural Design\n• Factored Load Combination: U = 1.2 D + 1.6 L\n• Equivalent Rectangular Stress Block Depth: a = (A_s · f_y) / (0.85 · f\'c · b)\n• Neutral Axis Depth: c = a / β₁\n• Nominal Moment Strength: M_n = A_s · f_y · (d - a/2)\n• Design Requirement: φ·M_n ≥ M_u  (with φ = 0.90 for tension-controlled beams)' },
        { page: 124, title: 'Chapter 9: Shear Strength & Stirrup Spacing in RC Beams', body: '9.1 Concrete + Web Reinforcement Shear Capacity\n• Concrete Shear Contribution: V_c = 0.17 · λ · √(f\'c) · b_w · d\n• Steel Stirrup Spacing: s = (A_v · f_yt · d) / V_s\n• Combined Design Check: φ(V_c + V_s) ≥ V_u  (with φ_shear = 0.75)' }
      ]
    },
    {
      id: 'geotech_terzaghi', title: 'Theoretical Soil Mechanics & Foundation Engineering', author: 'Karl Terzaghi',
      subjectKey: 'GEOTECHNICAL', courseCode: 'CE 315', year: 1943, totalPages: 528, isStarter: true,
      coverUrl: './icons/cover-geotech.jpg',
      pdfUrl: 'https://archive.org/download/theoreticalsoilm00terz/theoreticalsoilm00terz.pdf',
      description: 'Foundational geotechnical engineering treatise by Karl Terzaghi covering soil phase relationships, effective stress, Mohr-Coulomb shear strength, Rankine earth pressures, bearing capacity, and consolidation.',
      chapters: [
        { page: 1, title: 'Cover & Title Monograph — Theoretical Soil Mechanics (Karl Terzaghi)', body: 'THEORETICAL SOIL MECHANICS\nBy Karl Terzaghi\n\nThe landmark CE 315 geotechnical textbook on effective stress, seepage, earth pressure, bearing capacity, and consolidation settlement.' },
        { page: 28, title: 'Chapter 2: Soil Phase Relationships & Effective Stress Principle', body: '2.1 Fundamental Soil Weight-Volume Identities\n• Void Ratio: e = V_v / V_s   |   Porosity: n = e / (1 + e)\n• Fundamental Identity: S · e = w · G_s\n• Dry Unit Weight: γ_d = γ / (1 + w) = (G_s · γ_w) / (1 + e)\n\nTerzaghi’s Effective Stress Principle:\nσ\' = σ - u\nwhere σ = total vertical stress and u = γ_w · h_w = pore water pressure.' },
        { page: 50, title: 'Chapter 5 & 8: Mohr-Coulomb Shear Strength, Rankine Earth Pressure & Bearing Capacity', body: '5.1 Mohr-Coulomb Failure Criterion\nτ_f = c\' + σ\' · tan(φ\')\n\nRankine Active & Passive Earth Pressure Coefficients:\n• K_a = (1 - sin φ) / (1 + sin φ) = tan²(45° - φ/2)\n• K_p = (1 + sin φ) / (1 - sin φ) = tan²(45° + φ/2)\n\n8.1 Terzaghi Ultimate Bearing Capacity (Strip Footing):\nq_u = c\'·N_c + q·N_q + 0.5·γ·B·N_γ' }
      ]
    },
    {
      id: 'transpo_agg', title: 'Construction of Roads and Pavements & Highway Engineering', author: 'T. R. Agg',
      subjectKey: 'TRANSPORTATION', courseCode: 'CE 330', year: 1940, totalPages: 498, isStarter: true,
      coverUrl: './icons/cover-geotech.jpg',
      pdfUrl: 'https://archive.org/download/constructionofro00aggtuoft/constructionofro00aggtuoft.pdf',
      description: 'Highway and transportation engineering textbook on sight distance, horizontal superelevation, parabolic vertical curves, traffic flow, and flexible/rigid pavement design.',
      chapters: [
        { page: 1, title: 'Cover & Title Monograph — Highway & Pavement Engineering', body: 'CONSTRUCTION OF ROADS AND PAVEMENTS\nBy T. R. Agg\n\nCE 330 highway geometric design, stopping sight distance, superelevation, and AASHTO pavement thickness design.' },
        { page: 40, title: 'Chapter 3: Stopping Sight Distance & Horizontal Curve Superelevation', body: '3.1 Highway Geometric Design Formulas\n• Stopping Sight Distance: SSD = 0.278·V·t + V² / [254 · (f ± G)]\n• Horizontal Curve Equilibrium: e + f_s = V² / (127 · R)\n  (where V is design speed in km/h, R is radius in meters, e is superelevation, f_s is side friction).' }
      ]
    },
    {
      id: 'env_turneaure', title: 'Public Water Supplies & Sanitary Environmental Engineering', author: 'F. E. Turneaure & H. L. Russell',
      subjectKey: 'ENVIRONMENTAL', courseCode: 'CE 402', year: 1948, totalPages: 720, isStarter: true,
      coverUrl: './icons/cover-hydraulics.jpg',
      pdfUrl: 'https://archive.org/download/publicwatersuppl00turnuoft/publicwatersuppl00turnuoft.pdf',
      description: 'Sanitary and environmental engineering monograph on groundwater hydrology, coagulation, sedimentation, rapid sand filtration, disinfection, and wastewater BOD kinetics.',
      chapters: [
        { page: 1, title: 'Cover & Title Monograph — Public Water Supplies & Environmental Engineering', body: 'PUBLIC WATER SUPPLIES & SANITARY ENGINEERING\nBy F. E. Turneaure & H. L. Russell\n\nCE 402 textbook on water treatment plant design, Rational Method storm runoff, and BOD wastewater kinetics.' },
        { page: 52, title: 'Chapter 6: Rational Method Runoff & BOD Kinetics', body: '6.1 Peak Stormwater Runoff (Rational Formula)\nQ = C · I · A   (or Q = 0.278 · C · I · A in SI units)\n\nBiochemical Oxygen Demand (BOD) Kinetics:\nBOD_t = L_0 · (1 - e^(-k·t))' }
      ]
    },
    {
      id: 'const_walker', title: 'The Building Estimator’s Reference Book & Project Management', author: 'Frank R. Walker',
      subjectKey: 'CONSTRUCTION', courseCode: 'CE 408', year: 1957, totalPages: 640, isStarter: true,
      coverUrl: './icons/cover-structural.jpg',
      pdfUrl: 'https://archive.org/download/buildingestimato00walk/buildingestimato00walk.pdf',
      description: 'Construction engineering and estimating handbook covering concrete/rebar quantity takeoffs, Critical Path Method (CPM) scheduling, Earned Value Management, and engineering economy.',
      chapters: [
        { page: 1, title: 'Cover & Title Monograph — Building Estimator’s Reference & CPM Management', body: 'THE BUILDING ESTIMATOR’S REFERENCE BOOK\nBy Frank R. Walker\n\nCE 408 guide to concrete/masonry quantity takeoffs, CPM network scheduling, and Earned Value cost control.' },
        { page: 38, title: 'Chapter 4: Critical Path Method (CPM) & Earned Value Management', body: '4.1 CPM Network Scheduling\n• Early Finish: EF = ES + Duration\n• Late Start: LS = LF - Duration\n• Total Float: TF = LS - ES = LF - EF  (Critical Path has TF = 0)\n\nEarned Value Management:\n• Cost Performance Index: CPI = EV / AC\n• Schedule Performance Index: SPI = EV / PV' }
      ]
    },
    {
      id: 'masters_plates_timoshenko', title: 'Theory of Plates and Shells (Masters / Graduate Monograph)', author: 'S. Timoshenko & S. Woinowsky-Krieger',
      subjectKey: 'MASTERS_GRADUATE', courseCode: 'MSCE 501', year: 1959, totalPages: 594, isStarter: true,
      coverUrl: './icons/cover-structural.jpg',
      pdfUrl: 'https://archive.org/download/TheoryOfPlatesAndShells/Theory%20of%20Plates%20and%20Shells.pdf',
      description: 'Graduate MSCE monograph on Kirchhoff plate bending theory, biharmonic differential equation Δ⁴w = q/D, cylindrical/spherical thin shells, and finite element plate formulations.',
      chapters: [
        { page: 1, title: 'Cover & Title Monograph — Theory of Plates and Shells (Masters MSCE 501)', body: 'THEORY OF PLATES AND SHELLS\nBy S. Timoshenko & S. Woinowsky-Krieger\n\nMasters-level treatise on thin-plate flexure, Navier & Lévy series solutions, and shell membrane/bending theory.' },
        { page: 50, title: 'Chapter 4: Pure Bending & Biharmonic Equation of Thin Plates', body: '4.1 Kirchhoff-Love Plate Theory\nFlexural Rigidity of a Plate of thickness h:\nD = (E · h³) / [12 · (1 - ν²)]\n\nGoverning Biharmonic Differential Equation under transverse load q(x,y):\n∂⁴w/∂x⁴ + 2(∂⁴w/∂x²∂y²) + ∂⁴w/∂y⁴ = q(x,y) / D' }
      ]
    },
    {
      id: 'masters_dynamics_timoshenko', title: 'Vibration Problems in Engineering & Structural Dynamics', author: 'S. Timoshenko, D. H. Young & W. Weaver',
      subjectKey: 'MASTERS_GRADUATE', courseCode: 'MSCE 510', year: 1974, totalPages: 536, isStarter: true,
      coverUrl: './icons/cover-hydraulics.jpg',
      pdfUrl: 'https://archive.org/download/VibrationProblemsInEngineering/Vibration%20Problems%20in%20Engineering.pdf',
      description: 'Graduate earthquake and structural dynamics monograph covering SDOF/MDOF systems, Duhamel integral, modal superposition, response spectra, and continuous beam vibrations.',
      chapters: [
        { page: 1, title: 'Cover & Title Monograph — Vibration Problems in Engineering & Seismic Dynamics', body: 'VIBRATION PROBLEMS IN ENGINEERING\nBy S. Timoshenko, D. H. Young & W. Weaver\n\nGraduate MSCE 510 reference for SDOF/MDOF seismic response, modal analysis, and base shear design.' },
        { page: 44, title: 'Chapter 3: Multi-Degree-of-Freedom (MDOF) Eigenvalue & Seismic Modal Analysis', body: '3.1 MDOF Matrix Equation of Motion under Ground Acceleration ü_g(t):\n[M]{ü} + [C]{u̇} + [K]{u} = -[M]{1} ü_g(t)\n\nUndamped Natural Frequencies & Mode Shapes:\n|[K] - ω_n² [M]| = 0   →   ([K] - ω_n² [M]){φ_n} = {0}' }
      ]
    }
  ];

  // --- 3. PERSISTENT STATE (Always-On Device Cache in localStorage) ---
  const STORAGE_KEY = 'civlib_pwa_state_v2';
  function loadState() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) {
        const parsed = JSON.parse(raw);
        return Object.assign(getDefaultState(), parsed);
      }
    } catch (_) {}
    return getDefaultState();
  }

  function getDefaultState() {
    return {
      onboardingCompleted: false,
      profile: {
        displayName: '',
        courseProgram: COURSE_PROGRAMS[0],
        curriculumYear: 1,
        selectedSubjectKeys: ['MECHANICS', 'MATH_PHYSICS'],
        selectedGoals: ['Current Semester Courses'],
        studyMantra: 'BSCE · Mechanics, Math & Physics · Semester Track'
      },
      preferences: {
        paperTheme: 'PARCHMENT',
        fontSizePx: 17,
        readingLayout: 'SINGLE_PAGE',
        cropPdfMargins: true,
        autoSyncOnLaunch: true,
        resetToPortraitTrimmedOnOpen: true,
        customGeminiApiKey: ''
      },
      savedBookIds: ['calc_piskunov', 'mech_timoshenko', 'strength_timoshenko', 'hydraulics_king', 'struct_hool', 'geotech_terzaghi'],
      completedBookIds: [],
      readingProgress: {},
      studyNotes: [
        { id: 'note_1', bookId: 'strength_timoshenko', bookTitle: 'Strength of Materials (Part I)', pageNumber: 50, chapterTitle: 'Shear & Flexure Formula', noteText: 'Flexure Formula: σ = M·y / I and Shear: τ = V·Q / (I·b). Max bending moment occurs where V(x) = 0.' }
      ],
      extraBooks: []
    };
  }

  let state = loadState();
  function saveState() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
    } catch (_) {}
  }

  // Runtime UI state (not persisted)
  const ui = {
    activeTab: 'discover', // 'profile' | 'library' | 'bookmarks' | 'discover'
    profileSubTab: 1, // 0 = My Shelf, 1 = Recommend
    profileSubjectFilter: null,
    librarySubjectFilter: null,
    searchQuery: '',
    isSyncing: false,
    showSurveyModal: !state.onboardingCompleted,
    surveyStep: 0,
    showSettingsModal: false,
    showInstallGuideModal: false,
    detailBookId: null,
    showcaseShowRealCover: false,
    readerBookId: null,
    readerPageNumber: 1,
    readerShowChrome: true,
    readerShowHamburger: false,
    readerShowThumbnails: false,
    readerShowNoteDialog: false,
    readerShowJumpDialog: false,
    aiAssistantBookId: null,
    aiMessages: [],
    aiIsThinking: false,
    localPdfBlobs: {} // bookId -> { pdfDoc, numPages, pageTexts }
  };

  function getAllBooks() {
    const merged = [...STARTER_BOOKS];
    for (const eb of (state.extraBooks || [])) {
      if (!merged.some((b) => b.id === eb.id)) merged.push(eb);
    }
    return merged.map((b) => Object.assign({}, b, {
      currentPage: state.readingProgress[b.id] || 1,
      isSaved: state.savedBookIds.includes(b.id),
      isCompleted: state.completedBookIds.includes(b.id)
    }));
  }

  // --- 4. MULTI-SELECT SURVEY RECOMMENDATION SCORING ENGINE ---
  function scoreBookForProfile(book, profile) {
    if (!profile) return 0;
    let score = 0;
    const subj = SUBJECTS[book.subjectKey] || SUBJECTS.STRUCTURAL;
    const chosenSubjs = profile.selectedSubjectKeys || ['MECHANICS'];
    const chosenGoals = profile.selectedGoals || ['Current Semester Courses'];
    const targetYear = Math.min(5, Math.max(1, profile.curriculumYear || 1));

    const subjIdx = chosenSubjs.indexOf(book.subjectKey);
    if (subjIdx >= 0) {
      score += 120 - Math.min(35, subjIdx * 5);
    }
    if (subj.year === targetYear) score += 45;

    const mantraLower = (profile.studyMantra || '').toLowerCase();
    if (mantraLower.includes('masters') || mantraLower.includes('msce') || targetYear === 5) {
      if (['MASTERS_GRADUATE', 'STRUCTURAL', 'GEOTECHNICAL'].includes(book.subjectKey)) score += 30;
    }
    if (mantraLower.includes('allied') || mantraLower.includes('mechanical')) {
      if (['ALLIED_ENGINEERING', 'MATH_PHYSICS', 'MECHANICS'].includes(book.subjectKey)) score += 30;
    }

    for (const g of chosenGoals) {
      const gl = g.toLowerCase();
      if (gl.includes('semester') && subj.year === targetYear) score += 30;
      if (gl.includes('licensure') || gl.includes('board')) {
        if (['MECHANICS', 'MATERIALS', 'STRUCTURAL', 'HYDRAULICS', 'GEOTECHNICAL', 'SURVEYING', 'MATH_PHYSICS'].includes(book.subjectKey)) score += 25;
      }
      if (gl.includes('structural') || gl.includes('foundation')) {
        if (['STRUCTURAL', 'GEOTECHNICAL', 'MATERIALS', 'MASTERS_GRADUATE'].includes(book.subjectKey)) score += 35;
      }
      if (gl.includes('masters') || gl.includes('fem')) {
        if (['MASTERS_GRADUATE', 'STRUCTURAL', 'GEOTECHNICAL', 'MATH_PHYSICS'].includes(book.subjectKey)) score += 40;
      }
      if (gl.includes('math') || gl.includes('physics') || gl.includes('allied')) {
        if (['MATH_PHYSICS', 'ALLIED_ENGINEERING', 'MECHANICS'].includes(book.subjectKey)) score += 40;
      }
    }
    if (book.isStarter) score += 12;
    return score;
  }

  // --- 5. ANIMATED LAUNCH INTRO (Nothing -> "CivLib" -> "Civil Library") ---
  function runLaunchIntro() {
    const splash = document.getElementById('intro-splash');
    const icon = document.getElementById('intro-icon');
    const title = document.getElementById('intro-title');
    const sub = document.getElementById('intro-subtitle');
    if (!splash) return;

    let finished = false;
    window.skipIntro = function () {
      if (finished) return;
      finished = true;
      splash.classList.add('fade-out');
      setTimeout(() => { splash.style.display = 'none'; }, 380);
    };

    setTimeout(() => { if (icon) icon.classList.add('entered'); }, 100);
    setTimeout(() => { if (!finished && title) title.textContent = 'CivLib'; }, 420);
    setTimeout(() => {
      if (!finished && title) {
        title.textContent = 'Civil Library';
        if (sub) sub.classList.add('visible');
      }
    }, 1180);
    setTimeout(() => { window.skipIntro(); }, 2150);
  }

  // --- 6. TOAST SNACKBAR ---
  let toastTimer = null;
  function showToast(msg) {
    const bar = document.getElementById('toast-bar');
    if (!bar) return;
    bar.textContent = msg;
    bar.classList.add('visible');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => bar.classList.remove('visible'), 3000);
  }

  // --- 7. AUTO-SYNC OPEN-ACCESS ENGINEERING CATALOG ON APP OPEN ---
  async function syncOpenAccessCatalog(silent) {
    if (ui.isSyncing) return;
    ui.isSyncing = true;
    render();
    try {
      const resp = await fetch('https://openlibrary.org/search.json?q=civil+engineering+structural+hydraulics+calculus&limit=8');
      if (resp.ok) {
        const data = await resp.json();
        const docs = data.docs || [];
        let added = 0;
        for (const d of docs) {
          const id = 'ol_' + String(d.key || '').replace(/[^a-zA-Z0-9]/g, '_');
          if (!id || state.extraBooks.some((b) => b.id === id) || STARTER_BOOKS.some((b) => b.id === id)) continue;
          const title = d.title || 'Engineering Reference Volume';
          const author = (d.author_name && d.author_name[0]) || 'Academic Press';
          const subjKey = inferSubjectKey(title);
          const subj = SUBJECTS[subjKey] || SUBJECTS.STRUCTURAL;
          state.extraBooks.push({
            id: id,
            title: title,
            author: author,
            subjectKey: subjKey,
            courseCode: subj.code,
            year: d.first_publish_year || 1975,
            totalPages: d.number_of_pages_median || 420,
            isStarter: false,
            coverUrl: d.cover_i ? `https://covers.openlibrary.org/b/id/${d.cover_i}-M.jpg` : './icons/cover-structural.jpg',
            description: `Open-access engineering reference indexed for ${subj.name} (${subj.code}).`,
            chapters: [
              { page: 1, title: `Cover & Overview — ${title}`, body: `${title.toUpperCase()}\nBy ${author}\n\nCourse Classification: ${subj.code} · ${subj.name}\n\nUse the AI Study Assistant in the Hamburger Menu (☰) to ask questions, locate topics, or save formulas to your Study Notes.` }
            ]
          });
          added++;
        }
        saveState();
        if (!silent) {
          showToast(added > 0 ? `Synced ${added} new engineering volumes` : 'Engineering library catalog is up to date');
        }
      }
    } catch (_) {
      if (!silent) showToast('Offline mode — using cached engineering catalog');
    } finally {
      ui.isSyncing = false;
      render();
    }
  }

  function inferSubjectKey(text) {
    const l = (text || '').toLowerCase();
    if (l.includes('calculus') || l.includes('math') || l.includes('physics') || l.includes('differential')) return 'MATH_PHYSICS';
    if (l.includes('finite element') || l.includes('seismic') || l.includes('earthquake') || l.includes('plates') || l.includes('vibration')) return 'MASTERS_GRADUATE';
    if (l.includes('electrical') || l.includes('thermo') || l.includes('circuit') || l.includes('mechanical')) return 'ALLIED_ENGINEERING';
    if (l.includes('statics') || l.includes('dynamics') || l.includes('mechanics')) return 'MECHANICS';
    if (l.includes('survey') || l.includes('geomatics')) return 'SURVEYING';
    if (l.includes('strength') || l.includes('material')) return 'MATERIALS';
    if (l.includes('hydraulic') || l.includes('fluid') || l.includes('hydrology')) return 'HYDRAULICS';
    if (l.includes('soil') || l.includes('geotech') || l.includes('foundation')) return 'GEOTECHNICAL';
    if (l.includes('highway') || l.includes('transport') || l.includes('road')) return 'TRANSPORTATION';
    if (l.includes('water supply') || l.includes('sanitary') || l.includes('environmental')) return 'ENVIRONMENTAL';
    if (l.includes('construction') || l.includes('estimat') || l.includes('project')) return 'CONSTRUCTION';
    return 'STRUCTURAL';
  }

  // --- 8. EDITORIAL BOOK COVER COMPONENT (Always Default Editorial unless showRealCover=true) ---
  function renderBookCoverHTML(book, opts = {}) {
    const subj = SUBJECTS[book.subjectKey] || SUBJECTS.STRUCTURAL;
    const isMosaic = !!opts.mosaic;
    const showReal = !!opts.showRealCover && !!book.coverUrl;
    const onClickAttr = opts.onClick || `window.civlib.openBookClick('${book.id}')`;

    return `
      <div class="book-cover ${isMosaic ? 'mosaic-tile' : ''}" style="background:${subj.color};" onclick="${onClickAttr}">
        <div class="book-hinge"></div>
        <div class="book-fore-edge"></div>
        <div class="book-offline-pip" title="Available in Study Cache"></div>
        ${showReal ? `<img class="book-real-img" src="${escapeHtml(book.coverUrl)}" alt="${escapeHtml(book.title)}" onerror="this.style.display='none'" />` : ''}
        <div style="position:relative;z-index:2;display:${showReal ? 'none' : 'flex'};flex-direction:column;justify-content:space-between;height:100%;">
          <div>
            <div class="cover-top-meta">${escapeHtml(book.courseCode)} · Y${subj.year === 5 ? 'MS' : subj.year}</div>
            <div class="cover-rule"></div>
            <div class="cover-title">${escapeHtml(book.title)}</div>
          </div>
          <div>
            <div class="cover-rule"></div>
            <div class="cover-author">${escapeHtml(book.author)}</div>
          </div>
        </div>
      </div>
    `;
  }

  // --- 9. PAGE CONTENT GENERATOR FOR ANY PAGE (1..totalPages) ---
  function getPageDataForBook(book, pageNum) {
    const localBlob = ui.localPdfBlobs[book.id];
    if (localBlob && localBlob.pageTexts && localBlob.pageTexts[pageNum]) {
      return {
        pageNumber: pageNum,
        title: `Page ${pageNum} · ${book.title}`,
        body: localBlob.pageTexts[pageNum],
        hasCanvasPdf: true
      };
    }
    const chapters = book.chapters || [];
    const exact = chapters.find((c) => c.page === pageNum);
    if (exact) {
      return { pageNumber: pageNum, title: exact.title, body: exact.body, hasCanvasPdf: false };
    }
    // Find nearest preceding chapter
    let activeChap = chapters[0] || { page: 1, title: book.title, body: book.description };
    for (const c of chapters) {
      if (c.page <= pageNum) activeChap = c;
    }
    const subj = SUBJECTS[book.subjectKey] || SUBJECTS.STRUCTURAL;
    return {
      pageNumber: pageNum,
      title: `${activeChap.title} (Sec. ${pageNum})`,
      body: `${activeChap.body}\n\n────────────────────────────────────\n[${book.courseCode} · Page ${pageNum} of ${book.totalPages}]\nSubject Track: ${subj.name}\nAuthor: ${book.author}\n\nTip: Open the Hamburger Menu (☰) → AI Study Assistant to ask questions about Page ${pageNum}, find formulas in this textbook, or save a summary to your Study Notes.`
    };
  }

  // --- 10. AI STUDY ASSISTANT ENGINE (Free Pollinations LLM + Optional Gemini Key + Page Finder) ---
  async function askAiAssistant(book, question) {
    const q = (question || '').trim();
    if (!q) return;
    ui.aiMessages.push({ id: 'u_' + Date.now(), isUser: true, text: q, suggestedPages: [] });
    ui.aiIsThinking = true;
    render();

    // Scan book chapters & pages for matching topic page numbers
    const lowerQ = q.toLowerCase();
    const tokens = lowerQ.split(/\s+/).filter((t) => t.length > 2);
    const hits = [];
    for (const ch of (book.chapters || [])) {
      const hay = `${ch.title} ${ch.body}`.toLowerCase();
      let matchScore = 0;
      for (const tok of tokens) {
        if (hay.includes(tok)) matchScore += 10;
      }
      if (matchScore > 0) hits.push({ page: ch.page, title: ch.title, body: ch.body, score: matchScore });
    }
    // Topic keyword map (e.g., bending, shear, truss, manning, bearing, biharmonic, calculus)
    if (hits.length === 0) {
      if (lowerQ.includes('shear') || lowerQ.includes('moment') || lowerQ.includes('bending') || lowerQ.includes('truss') || lowerQ.includes('manning') || lowerQ.includes('bearing') || lowerQ.includes('plate')) {
        hits.push({ page: 50, title: `Core ${book.courseCode} Design & Formula Reference`, body: getPageDataForBook(book, 50).body, score: 25 });
      } else {
        const curP = ui.readerPageNumber || 1;
        const curData = getPageDataForBook(book, curP);
        hits.push({ page: curP, title: curData.title, body: curData.body, score: 15 });
      }
    }
    hits.sort((a, b) => b.score - a.score);
    const topHits = hits.slice(0, 2);
    const suggestedPages = [...new Set(topHits.map((h) => h.page))];
    const contextText = topHits.map((h) => `[Page ${h.page}: ${h.title}]\n${h.body}`).join('\n\n');

    const systemPrompt = `You are the CivLib AI Engineering Study Assistant for the textbook "${book.title}" (${book.courseCode}) by ${book.author}. The student is currently on Page ${ui.readerPageNumber || 1}. Use these indexed pages from the book:\n${contextText}\n\nAlways cite the exact Page Number (e.g., Page ${suggestedPages[0] || 50}), explain clearly with formulas, and keep it concise.`;

    let replyText = null;
    const customKey = (state.preferences.customGeminiApiKey || '').trim();

    if (customKey) {
      try {
        const gResp = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=${encodeURIComponent(customKey)}`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ contents: [{ parts: [{ text: `${systemPrompt}\n\nStudent Question: ${q}` }] }] })
        });
        if (gResp.ok) {
          const gData = await gResp.json();
          replyText = gData?.candidates?.[0]?.content?.parts?.[0]?.text;
        }
      } catch (_) {}
    }

    if (!replyText) {
      try {
        const pResp = await fetch('https://text.pollinations.ai/openai', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            model: 'openai',
            messages: [
              { role: 'system', content: systemPrompt },
              { role: 'user', content: q }
            ],
            temperature: 0.3
          })
        });
        if (pResp.ok) {
          const pData = await pResp.json();
          replyText = pData?.choices?.[0]?.message?.content;
        }
      } catch (_) {}
    }

    if (!replyText) {
      const targetP = suggestedPages[0] || 50;
      const hit = topHits[0];
      replyText = `Yes! In "${book.title}" (${book.courseCode}), this topic is covered on Page ${targetP} (${hit.title}):\n\n${hit.body}\n\nYou can tap "Open Page ${targetP} →" below to jump straight there, or save this summary to your Study Notes.`;
    }

    ui.aiIsThinking = false;
    ui.aiMessages.push({
      id: 'ai_' + Date.now(),
      isUser: false,
      text: replyText.replace(/\*\*/g, ''),
      suggestedPages: suggestedPages,
      noteSummary: replyText.replace(/\*\*/g, '').slice(0, 320),
      noteSavedForPage: null
    });
    render();
    const chatBox = document.getElementById('ai-chat-scroll');
    if (chatBox) chatBox.scrollTop = chatBox.scrollHeight;
  }

  // --- 11. SCREEN RENDERERS ---
  function render() {
    const root = document.getElementById('app-root');
    if (!root) return;

    // 1. Full-Screen Survey Modal (if onboarding not completed or user tapped Survey)
    if (ui.showSurveyModal) {
      root.innerHTML = renderSurveyModalHTML();
      return;
    }

    // 2. Full-Screen AI Study Assistant Window
    if (ui.aiAssistantBookId) {
      const book = getAllBooks().find((b) => b.id === ui.aiAssistantBookId) || STARTER_BOOKS[0];
      root.innerHTML = renderAiAssistantHTML(book);
      return;
    }

    // 3. Full-Screen Book Reader (Portrait Trimmed Default + Auto-Adjust Top/Bottom Bars + 2-Page Landscape)
    if (ui.readerBookId) {
      const book = getAllBooks().find((b) => b.id === ui.readerBookId) || STARTER_BOOKS[0];
      root.innerHTML = renderReaderScreenHTML(book);
      renderPdfCanvasIfAvailable(book, ui.readerPageNumber);
      return;
    }

    // 4. Book Detail / Showcase Screen (Editorial Cover by Default, Tap to Toggle Real Cover)
    if (ui.detailBookId) {
      const book = getAllBooks().find((b) => b.id === ui.detailBookId) || STARTER_BOOKS[0];
      root.innerHTML = renderBookDetailHTML(book);
      return;
    }

    // 5. Top-Level 4 Tabs: Profile | Library | Bookmarks | Home
    let screenHTML = '';
    if (ui.activeTab === 'profile') screenHTML = renderProfileScreenHTML();
    else if (ui.activeTab === 'library') screenHTML = renderLibraryScreenHTML();
    else if (ui.activeTab === 'bookmarks') screenHTML = renderBookmarksScreenHTML();
    else screenHTML = renderDiscoverScreenHTML();

    root.innerHTML = `
      <div class="screen-scroll">${screenHTML}</div>
      ${renderBottomNavHTML()}
      ${ui.showSettingsModal ? renderSettingsModalHTML() : ''}
      ${ui.showInstallGuideModal ? renderIosInstallGuideModalHTML() : ''}
    `;
  }

  function renderDiscoverScreenHTML() {
    const allBooks = getAllBooks();
    const q = (ui.searchQuery || '').trim().toLowerCase();
    const filtered = q
      ? allBooks.filter((b) => {
          const s = SUBJECTS[b.subjectKey] || SUBJECTS.STRUCTURAL;
          return `${b.title} ${b.author} ${b.courseCode} ${s.name} ${s.short} ${b.description}`.toLowerCase().includes(q);
        })
      : allBooks;

    const recommended = [...allBooks].sort((a, b) => scoreBookForProfile(b, state.profile) - scoreBookForProfile(a, state.profile));
    const featured = recommended[0] || allBooks[0];
    const pName = state.profile.displayName || 'Heart Garcia';
    const yLabel = state.profile.curriculumYear === 5 ? 'Masters / MSCE' : `Year ${state.profile.curriculumYear}`;

    return `
      <div style="padding: calc(16px + var(--safe-top)) 18px 24px;">
        <!-- Top Header -->
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:14px;">
          <div>
            <div style="font-size:11px;font-weight:700;letter-spacing:1.2px;color:var(--sage-olive);text-transform:uppercase;">
              ${escapeHtml(pName)} · ${escapeHtml(yLabel)}
            </div>
            <h1 class="serif-title" style="font-size:26px;color:var(--sand-cream-light);">Civil Library</h1>
          </div>
          <div style="display:flex;gap:8px;">
            <button class="pill-btn pill-sage" onclick="window.civlib.openInstallGuide()">iOS App Guide</button>
            <button class="pill-btn pill-cream" onclick="window.civlib.triggerPdfImport()">+ Import PDF</button>
          </div>
        </div>

        <!-- Instant Local + Catalog Search Bar -->
        <div style="position:relative;margin-bottom:14px;">
          <input
            type="text"
            value="${escapeHtml(ui.searchQuery)}"
            oninput="window.civlib.onSearchInput(this.value)"
            placeholder="Search library first (title, CE 301, calculus, author…)"
            style="width:100%;padding:13px 16px;border-radius:16px;border:1px solid rgba(235,224,204,0.25);background:var(--espresso-card);color:var(--sand-cream-light);font-size:14px;outline:none;"
          />
        </div>

        <!-- Quick Subject Filter Pills -->
        <div class="chip-row" style="margin: 0 -18px 16px;">
          ${['Calculus', 'CE 101 Statics', 'Strength of Materials', 'Hydraulics', 'Structural RC', 'Soil Mechanics', 'Masters FEM'].map((chip) => `
            <button class="pill-btn pill-sage" onclick="window.civlib.onSearchInput('${chip}')">${chip}</button>
          `).join('')}
        </div>

        ${q ? `
          <div style="margin-bottom:18px;">
            <div style="font-size:12px;font-weight:700;color:var(--sand-cream);margin-bottom:10px;letter-spacing:0.8px;">
              IN YOUR LIBRARY (${filtered.length} MATCHES)
            </div>
            <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:10px;">
              ${filtered.map((b) => `
                <div>
                  ${renderBookCoverHTML(b)}
                  <div style="font-size:11.5px;font-weight:600;margin-top:5px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${escapeHtml(b.title)}</div>
                </div>
              `).join('')}
            </div>
          </div>
        ` : `
          <!-- Featured Recommended Textbook Card -->
          <div style="background:var(--sand-cream);color:var(--espresso-ink);border-radius:20px;padding:16px;margin-bottom:20px;display:flex;gap:14px;align-items:center;">
            <div style="width:92px;flex-shrink:0;">
              ${renderBookCoverHTML(featured)}
            </div>
            <div style="flex:1;min-width:0;">
              <div style="font-size:10.5px;font-weight:700;letter-spacing:0.8px;color:var(--espresso-ink-muted);text-transform:uppercase;">
                RECOMMENDED FOR YOUR SURVEY
              </div>
              <div class="serif-title" style="font-size:18px;font-weight:700;line-height:1.25;margin:4px 0;">
                ${escapeHtml(featured.title)}
              </div>
              <div style="font-size:12px;color:var(--espresso-ink-muted);margin-bottom:10px;">
                ${escapeHtml(featured.author)} · ${escapeHtml(featured.courseCode)}
              </div>
              <div style="display:flex;gap:8px;flex-wrap:wrap;">
                <button class="pill-btn pill-espresso" onclick="window.civlib.openReader('${featured.id}', ${featured.currentPage})">Read Now</button>
                <button class="pill-btn" style="background:rgba(28,21,18,0.12);color:var(--espresso-ink);border-color:rgba(28,21,18,0.22);" onclick="window.civlib.openAiForBook('${featured.id}')">✨ Ask AI</button>
              </div>
            </div>
          </div>

          <!-- Top Recommended Grid Based on Multi-Select Survey -->
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px;">
            <h2 class="serif-title" style="font-size:19px;">Recommended For You</h2>
            <span style="font-size:12px;color:var(--sand-cream);cursor:pointer;font-weight:600;" onclick="window.civlib.switchTab('profile')">Edit Survey →</span>
          </div>
          <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:11px;margin-bottom:22px;">
            ${recommended.slice(0, 9).map((b) => `
              <div>
                ${renderBookCoverHTML(b)}
                <div style="font-size:11.5px;font-weight:600;margin-top:5px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${escapeHtml(b.title)}</div>
                <div style="font-size:10px;color:var(--sand-cream-muted);">${escapeHtml(b.courseCode)}</div>
              </div>
            `).join('')}
          </div>
        `}
      </div>
    `;
  }

  function renderLibraryScreenHTML() {
    const allBooks = getAllBooks().sort((a, b) => {
      const sa = SUBJECTS[a.subjectKey] || SUBJECTS.STRUCTURAL;
      const sb = SUBJECTS[b.subjectKey] || SUBJECTS.STRUCTURAL;
      return sa.order - sb.order;
    });
    const activeSubj = ui.librarySubjectFilter;
    const filtered = activeSubj ? allBooks.filter((b) => b.subjectKey === activeSubj) : allBooks;

    return `
      <div style="padding: calc(16px + var(--safe-top)) 16px 24px;">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
          <div>
            <div style="font-size:11px;font-weight:700;color:var(--sage-olive);letter-spacing:1px;">ALL 12 ENGINEERING &amp; MASTERS TRACKS</div>
            <h1 class="serif-title" style="font-size:24px;">Engineering Library (${filtered.length})</h1>
          </div>
          <button class="pill-btn pill-cream" onclick="window.civlib.syncCatalog()">${ui.isSyncing ? 'Syncing…' : 'Sync'}</button>
        </div>

        <div class="chip-row" style="margin: 0 -16px 14px;">
          <button class="pill-btn ${!activeSubj ? 'pill-cream' : 'pill-card'}" onclick="window.civlib.setLibrarySubject(null)">All Subjects</button>
          ${Object.values(SUBJECTS).map((s) => `
            <button class="pill-btn ${activeSubj === s.key ? 'pill-cream' : 'pill-sage'}" onclick="window.civlib.setLibrarySubject('${s.key}')">
              ${escapeHtml(s.short)} (${escapeHtml(s.code)})
            </button>
          `).join('')}
        </div>

        <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:11px;">
          ${filtered.map((b) => `
            <div>
              ${renderBookCoverHTML(b)}
              <div style="font-size:11.5px;font-weight:600;margin-top:5px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${escapeHtml(b.title)}</div>
              <div style="font-size:10px;color:var(--sand-cream-muted);">${escapeHtml(b.courseCode)} · ${b.totalPages}p</div>
            </div>
          `).join('')}
        </div>
      </div>
    `;
  }

  function renderBookmarksScreenHTML() {
    const allBooks = getAllBooks();
    const saved = allBooks.filter((b) => state.savedBookIds.includes(b.id));
    const notes = state.studyNotes || [];

    return `
      <div style="padding: calc(16px + var(--safe-top)) 16px 24px;">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:14px;">
          <div>
            <div style="font-size:11px;font-weight:700;color:var(--sage-olive);letter-spacing:1px;">OFFLINE STUDY CACHE</div>
            <h1 class="serif-title" style="font-size:24px;">Saved Shelf &amp; Notes</h1>
          </div>
          <button class="pill-btn pill-cream" onclick="window.civlib.triggerPdfImport()">+ Import PDF</button>
        </div>

        <div style="font-size:13px;font-weight:700;color:var(--sand-cream);margin-bottom:10px;">
          SAVED TEXTBOOKS (${saved.length})
        </div>
        <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:11px;margin-bottom:24px;">
          ${saved.map((b) => `
            <div>
              ${renderBookCoverHTML(b)}
              <div style="font-size:11.5px;font-weight:600;margin-top:5px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${escapeHtml(b.title)}</div>
              <div style="font-size:10px;color:var(--sand-cream-muted);">Page ${b.currentPage} of ${b.totalPages}</div>
            </div>
          `).join('')}
        </div>

        <div style="font-size:13px;font-weight:700;color:var(--sand-cream);margin-bottom:10px;">
          STUDY NOTES (${notes.length})
        </div>
        <div style="display:flex;flex-direction:column;gap:8px;">
          ${notes.map((n) => `
            <div style="background:var(--espresso-card);border:1px solid rgba(235,224,204,0.16);border-radius:14px;padding:12px 14px;display:flex;justify-content:space-between;align-items:center;gap:10px;">
              <div style="flex:1;cursor:pointer;" onclick="window.civlib.openReader('${n.bookId}', ${n.pageNumber})">
                <div class="lora-text" style="font-size:13.5px;font-weight:600;color:var(--sand-cream-light);">
                  Page ${n.pageNumber} · ${escapeHtml(n.bookTitle)}
                </div>
                <div style="font-size:12px;color:var(--sand-cream-muted);margin-top:3px;">${escapeHtml(n.noteText)}</div>
              </div>
              <button class="pill-btn pill-card" onclick="window.civlib.deleteNote('${n.id}')">✕</button>
            </div>
          `).join('')}
        </div>
      </div>
    `;
  }

  function renderProfileScreenHTML() {
    const allBooks = getAllBooks();
    const saved = allBooks.filter((b) => state.savedBookIds.includes(b.id));
    const p = state.profile;
    const displayName = p.displayName || 'Heart Garcia';
    const yearLabel = p.curriculumYear === 5 ? 'Masters / Grad' : `Year ${p.curriculumYear}`;
    const chosenSubjs = (p.selectedSubjectKeys || ['MECHANICS']).map((k) => SUBJECTS[k] || SUBJECTS.MECHANICS);
    const chosenGoals = p.selectedGoals || ['Current Semester Courses'];

    let recommended = [...allBooks].sort((a, b) => scoreBookForProfile(b, p) - scoreBookForProfile(a, p));
    if (ui.profileSubjectFilter) {
      recommended = recommended.filter((b) => b.subjectKey === ui.profileSubjectFilter);
    }
    const gridBooks = ui.profileSubTab === 1 ? recommended : saved;

    return `
      <div>
        <!-- Upper Dark Espresso Band -->
        <div style="background:var(--espresso-bg);padding: calc(14px + var(--safe-top)) 20px 0;height: calc(116px + var(--safe-top));position:relative;">
          <div style="display:flex;justify-content:space-between;align-items:center;">
            <span style="font-size:14px;font-weight:600;color:var(--sand-cream-light);cursor:pointer;" onclick="window.civlib.syncCatalog()">
              ${ui.isSyncing ? 'Syncing…' : 'Sync'}
            </span>
            <span class="serif-title" style="font-size:24px;font-weight:600;color:var(--sand-cream-light);">Profile</span>
            <div style="display:flex;align-items:center;gap:10px;">
              <span style="font-size:13.5px;font-weight:700;color:var(--sand-cream);cursor:pointer;" onclick="window.civlib.openSurvey()">Survey</span>
              <button class="pill-btn pill-card" style="padding:5px 11px;" onclick="window.civlib.openSettings()">⚙ Settings</button>
            </div>
          </div>

          <!-- Centered Circular Student Avatar straddling Espresso / SageOlive seam -->
          <div style="position:absolute;left:50%;bottom:-56px;transform:translateX(-50%);width:112px;height:112px;border-radius:50%;border:3.5px solid var(--sage-olive);overflow:hidden;box-shadow:0 8px 20px rgba(0,0,0,0.45);background:var(--espresso-card);z-index:5;">
            <img src="./icons/icon-512.jpg" alt="Student Avatar" style="width:100%;height:100%;object-fit:cover;" />
          </div>
        </div>

        <!-- Lower Olive-Sage Profile Band -->
        <div style="background:var(--sage-olive);color:var(--espresso-ink);padding: 68px 18px 14px;text-align:center;">
          <div class="serif-title" style="font-size:25px;font-weight:700;">${escapeHtml(displayName)}</div>
          <div class="lora-text" style="font-size:13.5px;color:var(--espresso-ink-muted);margin-top:4px;cursor:pointer;" onclick="window.civlib.openSurvey()">
            ${escapeHtml(yearLabel)} · ${escapeHtml(p.studyMantra)} ✎
          </div>

          <!-- Survey + Settings + Goals Pills -->
          <div style="display:flex;justify-content:center;gap:8px;flex-wrap:wrap;margin-top:10px;">
            <button class="pill-btn pill-cream" onclick="window.civlib.openSurvey()">
              ✎ Survey · ${chosenSubjs.length} Focus ${chosenSubjs.length === 1 ? 'Subject' : 'Subjects'}
            </button>
            <button class="pill-btn pill-espresso" onclick="window.civlib.openSettings()">
              ⚙ Settings
            </button>
            <button class="pill-btn" style="background:rgba(28,21,18,0.12);color:var(--espresso-ink);" onclick="window.civlib.openSurvey()">
              ${chosenGoals.length} ${chosenGoals.length === 1 ? 'Goal' : 'Goals'}
            </button>
          </div>

          <!-- 3-Column Metrics Strip -->
          <div style="display:flex;justify-content:space-evenly;align-items:center;margin-top:14px;">
            <div>
              <div class="serif-title" style="font-size:18px;font-weight:700;">${saved.length}</div>
              <div style="font-size:11px;color:var(--espresso-ink-muted);">Saved</div>
            </div>
            <div style="width:1px;height:24px;background:rgba(28,21,18,0.18);"></div>
            <div>
              <div class="serif-title" style="font-size:18px;font-weight:700;">${state.completedBookIds.length}</div>
              <div style="font-size:11px;color:var(--espresso-ink-muted);">Completed</div>
            </div>
            <div style="width:1px;height:24px;background:rgba(28,21,18,0.18);"></div>
            <div>
              <div class="serif-title" style="font-size:18px;font-weight:700;">${(state.studyNotes || []).length}</div>
              <div style="font-size:11px;color:var(--espresso-ink-muted);">Notes</div>
            </div>
          </div>
        </div>

        <!-- Sticky 2-Tab Bar: My Shelf | Recommend -->
        <div style="background:var(--sage-olive);border-top:1px solid rgba(28,21,18,0.15);display:flex;">
          <div onclick="window.civlib.setProfileTab(0)" style="flex:1;text-align:center;padding:11px 8px;font-size:13px;font-weight:${ui.profileSubTab === 0 ? '700' : '500'};color:var(--espresso-ink);border-bottom:2.5px solid ${ui.profileSubTab === 0 ? 'var(--espresso-ink)' : 'transparent'};cursor:pointer;">
            My Shelf (${saved.length})
          </div>
          <div onclick="window.civlib.setProfileTab(1)" style="flex:1;text-align:center;padding:11px 8px;font-size:13px;font-weight:${ui.profileSubTab === 1 ? '700' : '500'};color:var(--espresso-ink);border-bottom:2.5px solid ${ui.profileSubTab === 1 ? 'var(--espresso-ink)' : 'transparent'};cursor:pointer;">
            Recommend
          </div>
        </div>

        ${ui.profileSubTab === 1 ? `
          <div class="chip-row" style="padding:10px 14px;background:var(--espresso-bg);">
            <button class="pill-btn ${!ui.profileSubjectFilter ? 'pill-cream' : 'pill-card'}" onclick="window.civlib.setProfileSubjectFilter(null)">
              Recommended For You · ${escapeHtml(yearLabel)}
            </button>
            ${chosenSubjs.map((s) => `
              <button class="pill-btn ${ui.profileSubjectFilter === s.key ? 'pill-cream' : 'pill-sage'}" onclick="window.civlib.setProfileSubjectFilter('${s.key}')">
                ${escapeHtml(s.short)} (${escapeHtml(s.code)})
              </button>
            `).join('')}
          </div>
        ` : ''}

        <!-- Edge-to-Edge 3-Column IG Book Mosaic (Always Custom Editorial Cover) -->
        <div class="ig-mosaic">
          ${gridBooks.map((b) => renderBookCoverHTML(b, { mosaic: true })).join('')}
        </div>
      </div>
    `;
  }

  // --- 12. BOOK DETAIL / SHOWCASE SCREEN (Editorial Cover Default + Tap to Toggle Real Cover + Ask AI) ---
  function renderBookDetailHTML(book) {
    const subj = SUBJECTS[book.subjectKey] || SUBJECTS.STRUCTURAL;
    const isSaved = state.savedBookIds.includes(book.id);
    const isDone = state.completedBookIds.includes(book.id);

    return `
      <div class="screen-scroll" style="padding: calc(14px + var(--safe-top)) 20px 36px;">
        <!-- Top Bar -->
        <div style="display:flex;justify-content:space-between;align-items:center;padding-bottom:12px;border-bottom:1px solid rgba(235,224,204,0.14);margin-bottom:18px;">
          <button class="pill-btn pill-card" onclick="window.civlib.closeDetail()">← Back</button>
          <div style="font-size:11.5px;font-weight:700;color:var(--sand-cream-muted);">
            YEAR ${subj.year === 5 ? 'MS' : subj.year} · ${escapeHtml(book.courseCode)} / ${escapeHtml(subj.short.toUpperCase())}
          </div>
          <div style="display:flex;gap:6px;">
            <button class="pill-btn ${isDone ? 'pill-cream' : 'pill-card'}" onclick="window.civlib.toggleCompleted('${book.id}')">${isDone ? '✓ Done' : 'Mark Done'}</button>
            <button class="pill-btn ${isSaved ? 'pill-cream' : 'pill-card'}" onclick="window.civlib.toggleSaved('${book.id}')">${isSaved ? '★ Saved' : '☆ Save'}</button>
          </div>
        </div>

        <!-- Showcase Row: Custom Editorial Cover by Default, Tap Cover to Toggle Real Book Cover -->
        <div style="display:flex;gap:18px;align-items:flex-start;margin-bottom:20px;">
          <div style="width:134px;flex-shrink:0;text-align:center;">
            ${renderBookCoverHTML(book, {
              showRealCover: ui.showcaseShowRealCover,
              onClick: `window.civlib.toggleShowcaseRealCover()`
            })}
            <div style="font-size:10.5px;color:var(--sand-cream);margin-top:6px;cursor:pointer;font-weight:600;" onclick="window.civlib.toggleShowcaseRealCover()">
              ${ui.showcaseShowRealCover ? 'Tap for CivLib cover' : 'Tap cover for real cover'}
            </div>
          </div>
          <div style="flex:1;">
            <div style="font-size:10.5px;font-weight:700;color:var(--brass);letter-spacing:0.8px;">FULL ENGINEERING MONOGRAPH</div>
            <h1 class="serif-title" style="font-size:21px;line-height:1.25;margin:6px 0;">${escapeHtml(book.title)}</h1>
            <div style="font-size:13.5px;color:var(--sand-cream-muted);margin-bottom:12px;">${escapeHtml(book.author)}</div>
            <div style="font-size:11px;font-weight:700;color:#62C394;">OFFLINE STUDY CACHE READY · ${book.totalPages} PAGES</div>
          </div>
        </div>

        <!-- Primary Action Buttons + Optional Compact Ask AI Button -->
        <div style="display:flex;gap:10px;flex-wrap:wrap;margin-bottom:20px;">
          <button class="pill-btn pill-cream" style="flex:1;padding:13px 18px;font-size:14px;" onclick="window.civlib.openReader('${book.id}', ${book.currentPage})">
            📖 Open Textbook (Page ${book.currentPage})
          </button>
          <button class="pill-btn pill-sage" style="padding:13px 16px;font-size:13.5px;" onclick="window.civlib.openAiForBook('${book.id}')">
            ✨ Ask AI
          </button>
        </div>

        <div style="background:var(--espresso-card);border:1px solid rgba(235,224,204,0.14);border-radius:16px;padding:16px;margin-bottom:18px;">
          <div style="font-size:11px;font-weight:700;color:var(--sand-cream);letter-spacing:0.8px;margin-bottom:6px;">VOLUME SYNOPSIS</div>
          <p class="lora-text" style="font-size:13.5px;line-height:1.6;color:var(--sand-cream-light);">${escapeHtml(book.description)}</p>
        </div>

        <!-- Indexed Chapters -->
        <div style="font-size:12px;font-weight:700;color:var(--sand-cream);letter-spacing:0.8px;margin-bottom:10px;">
          INDEXED CHAPTERS &amp; FORMULA SECTIONS
        </div>
        <div style="display:flex;flex-direction:column;gap:8px;">
          ${(book.chapters || []).map((ch) => `
            <div onclick="window.civlib.openReader('${book.id}', ${ch.page})" style="background:var(--espresso-card);border:1px solid rgba(235,224,204,0.14);border-radius:12px;padding:12px 14px;cursor:pointer;display:flex;justify-content:space-between;align-items:center;">
              <div>
                <div style="font-size:13px;font-weight:700;color:var(--sand-cream-light);">${escapeHtml(ch.title)}</div>
                <div style="font-size:11.5px;color:var(--sand-cream-muted);margin-top:2px;">Starts on Page ${ch.page}</div>
              </div>
              <span class="mono-text" style="font-size:11px;color:var(--sand-cream);">p.${ch.page} →</span>
            </div>
          `).join('')}
        </div>
      </div>
    `;
  }

  // --- 13. BOOK READER SCREEN (Portrait Trimmed Default + Auto-Adjust Bars + Single Hamburger Menu + Real Page Previews) ---
  function renderReaderScreenHTML(book) {
    const prefs = state.preferences;
    const isTwoPage = prefs.readingLayout === 'TWO_PAGE_SPREAD';
    const pageNum = Math.min(book.totalPages, Math.max(1, ui.readerPageNumber || 1));
    const pageData = getPageDataForBook(book, pageNum);
    const rightPageData = isTwoPage && pageNum + 1 <= book.totalPages ? getPageDataForBook(book, pageNum + 1) : null;
    const pct = Math.round((pageNum * 100) / Math.max(1, book.totalPages));
    const themeClass = `reader-theme-${prefs.paperTheme || 'PARCHMENT'}`;
    const isTrimmed = !!prefs.cropPdfMargins;
    const isPage1Cover = pageNum === 1;

    // Hamburger Dropdown Menu (Clean Labels/Titles ONLY — no secondary descriptions)
    const hamburgerDropdownHTML = ui.readerShowHamburger ? `
      <div style="position:fixed;inset:0;z-index:650;" onclick="window.civlib.toggleHamburger(false)"></div>
      <div style="position:absolute;top:48px;right:12px;z-index:660;width:240px;background:var(--reader-card);color:var(--reader-ink);border:1px solid var(--reader-border);border-radius:14px;box-shadow:0 12px 32px rgba(0,0,0,0.45);overflow:hidden;">
        <div onclick="window.civlib.openAiFromReader()" style="padding:13px 16px;font-weight:700;font-size:13.5px;display:flex;align-items:center;gap:10px;cursor:pointer;border-bottom:1px solid var(--reader-border);">
          <span>✨</span><span>AI Study Assistant</span>
        </div>
        <div onclick="window.civlib.openThumbnailsSheet()" style="padding:12px 16px;font-weight:600;font-size:13.5px;display:flex;align-items:center;gap:10px;cursor:pointer;">
          <span>⊞</span><span>Book Pages Preview</span>
        </div>
        <div onclick="window.civlib.openJumpPagePrompt()" style="padding:12px 16px;font-weight:600;font-size:13.5px;display:flex;align-items:center;gap:10px;cursor:pointer;">
          <span>#</span><span>Jump to Page Number</span>
        </div>
        <div onclick="window.civlib.quickBookmarkPage()" style="padding:12px 16px;font-weight:600;font-size:13.5px;display:flex;align-items:center;gap:10px;cursor:pointer;">
          <span>★</span><span>Bookmark This Page</span>
        </div>
        <div onclick="window.civlib.openAddNotePrompt()" style="padding:12px 16px;font-weight:600;font-size:13.5px;display:flex;align-items:center;gap:10px;cursor:pointer;">
          <span>✎</span><span>Add / Edit Study Note</span>
        </div>
        <div onclick="window.civlib.toggleSpreadLayout()" style="padding:12px 16px;font-weight:600;font-size:13.5px;display:flex;align-items:center;gap:10px;cursor:pointer;border-top:1px solid var(--reader-border);">
          <span>📖</span><span>${isTwoPage ? 'Switch to 1-Page Portrait' : 'Switch to 2-Page Landscape'}</span>
        </div>
      </div>
    ` : '';

    const renderSinglePageSheet = (pData, isCoverPage) => `
      <div
        onclick="window.civlib.toggleReaderChrome()"
        style="flex:1;height:100%;background:var(--reader-bg);color:var(--reader-ink);border:1px solid var(--reader-border);border-radius:8px;overflow-y:auto;padding:${isTrimmed ? '12px 14px' : '26px 28px'};display:flex;flex-direction:column;justify-content:space-between;box-shadow:0 4px 18px rgba(0,0,0,0.28);"
      >
        <div>
          <div style="display:flex;justify-content:space-between;align-items:center;border-bottom:1px solid var(--reader-border);padding-bottom:6px;margin-bottom:12px;font-size:11px;color:var(--reader-subtle);">
            <span class="mono-text">${escapeHtml(book.courseCode)} · ${isTrimmed ? 'TRIMMED' : 'ORIGINAL'}</span>
            <span class="mono-text">Page ${pData.pageNumber} of ${book.totalPages}</span>
          </div>

          ${isCoverPage && book.coverUrl ? `
            <!-- Actual Cover Page appears on Page 1 inside the open book -->
            <div style="text-align:center;margin-bottom:14px;">
              <img src="${escapeHtml(book.coverUrl)}" alt="Actual Book Cover Page" style="max-height:220px;border-radius:6px;box-shadow:0 4px 14px rgba(0,0,0,0.25);margin-bottom:10px;" />
            </div>
          ` : ''}

          <canvas id="pdf-canvas-p${pData.pageNumber}" style="display:none;width:100%;border-radius:4px;margin-bottom:10px;"></canvas>

          <h2 class="serif-title" style="font-size:${prefs.fontSizePx + 2}px;margin-bottom:10px;line-height:1.3;">
            ${escapeHtml(pData.title)}
          </h2>
          <div class="lora-text" style="font-size:${prefs.fontSizePx}px;line-height:1.68;white-space:pre-wrap;">${escapeHtml(pData.body)}</div>
        </div>

        <div style="border-top:1px solid var(--reader-border);padding-top:8px;margin-top:16px;display:flex;justify-content:space-between;font-size:11px;color:var(--reader-subtle);">
          <span>${escapeHtml(book.title)}</span>
          <span class="mono-text">p. ${pData.pageNumber}</span>
        </div>
      </div>
    `;

    // Portrait 1-Page Mode: Flexbox Column auto-adjusts page viewport between Top and Bottom bars when open!
    if (!isTwoPage) {
      return `
        <div class="fullscreen-overlay ${themeClass}" style="background:var(--reader-desk);">
          ${ui.readerShowChrome ? `
            <!-- Portrait Top Bar with Single Hamburger Icon (☰) -->
            <div style="position:relative;background:var(--reader-card);color:var(--reader-ink);border-bottom:1px solid var(--reader-border);padding: calc(10px + var(--safe-top)) 12px 10px;display:flex;align-items:center;justify-content:space-between;flex-shrink:0;">
              <button class="pill-btn" style="background:var(--reader-bg);color:var(--reader-ink);border-color:var(--reader-border);" onclick="window.civlib.closeReader()">← Back</button>
              <div style="flex:1;min-width:0;padding:0 10px;">
                <div style="font-size:13.5px;font-weight:700;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${escapeHtml(book.title)}</div>
                <div style="font-size:11px;color:var(--reader-subtle);">Page ${pageNum} of ${book.totalPages} (${pct}%)</div>
              </div>
              <button class="pill-btn" style="background:var(--reader-bg);color:var(--reader-ink);border-color:var(--reader-border);padding:8px 12px;font-size:15px;" onclick="window.civlib.toggleHamburger()">☰</button>
              ${hamburgerDropdownHTML}
            </div>
          ` : ''}

          <!-- Auto-Adjusting Portrait Page Viewport (Never covered by Top or Bottom bars) -->
          <div style="flex:1;min-height:0;padding:8px;display:flex;flex-direction:column;">
            ${renderSinglePageSheet(pageData, isPage1Cover)}
          </div>

          ${ui.readerShowChrome ? `
            <!-- Portrait Bottom Bar -->
            <div style="background:var(--reader-card);color:var(--reader-ink);border-top:1px solid var(--reader-border);padding:10px 14px calc(12px + var(--safe-bottom));flex-shrink:0;">
              <div style="display:flex;align-items:center;gap:10px;margin-bottom:8px;">
                <button class="pill-btn" style="background:var(--reader-bg);color:var(--reader-ink);border-color:var(--reader-border);" onclick="window.civlib.turnPage(-1)">◀</button>
                <input type="range" min="1" max="${book.totalPages}" value="${pageNum}" oninput="window.civlib.goToPage(Number(this.value))" style="flex:1;" />
                <button class="pill-btn" style="background:var(--reader-bg);color:var(--reader-ink);border-color:var(--reader-border);" onclick="window.civlib.turnPage(1)">▶</button>
              </div>
              <div style="display:flex;justify-content:space-between;align-items:center;gap:6px;flex-wrap:wrap;">
                <div style="display:flex;gap:5px;">
                  ${[
                    ['PARCHMENT', 'Book'],
                    ['CRISP', 'Crisp'],
                    ['BLUEPRINT_DARK', 'Night']
                  ].map(([tKey, tLabel]) => `
                    <button class="pill-btn" style="padding:6px 10px;border-radius:8px;background:${prefs.paperTheme === tKey ? '#7D3C27' : 'var(--reader-bg)'};color:${prefs.paperTheme === tKey ? '#FFF' : 'var(--reader-ink)'};border-color:var(--reader-border);" onclick="window.civlib.setPaperTheme('${tKey}')">${tLabel}</button>
                  `).join('')}
                </div>
                <div style="display:flex;gap:6px;">
                  <button class="pill-btn" style="padding:6px 10px;border-radius:8px;background:var(--reader-bg);color:var(--reader-ink);border-color:var(--reader-border);" onclick="window.civlib.toggleSpreadLayout()">1-Page</button>
                  <button class="pill-btn" style="padding:6px 10px;border-radius:8px;background:${isTrimmed ? '#7D3C27' : 'var(--reader-bg)'};color:${isTrimmed ? '#FFF' : 'var(--reader-ink)'};border-color:var(--reader-border);" onclick="window.civlib.toggleTrimMargins()">✂ Trim</button>
                </div>
              </div>
            </div>
          ` : ''}

          ${ui.readerShowThumbnails ? renderThumbnailsModalHTML(book, pageNum) : ''}
        </div>
      `;
    }

    // 2-Page Landscape Spread Mode: Left and Right Vertical Option Bars (Never covering the 2-page spread)
    return `
      <div class="fullscreen-overlay ${themeClass}" style="background:var(--reader-desk);flex-direction:row;">
        ${ui.readerShowChrome ? `
          <!-- Left Vertical Option Bar -->
          <div style="position:relative;width:58px;background:var(--reader-card);color:var(--reader-ink);border-right:1px solid var(--reader-border);padding:10px 6px;display:flex;flex-direction:column;justify-content:space-between;align-items:center;flex-shrink:0;">
            <div style="display:flex;flex-direction:column;gap:8px;align-items:center;">
              <button class="pill-btn" style="padding:7px;border-radius:10px;background:var(--reader-bg);color:var(--reader-ink);border-color:var(--reader-border);" onclick="window.civlib.closeReader()">←</button>
              <button class="pill-btn" style="padding:7px;border-radius:10px;background:var(--reader-bg);color:var(--reader-ink);border-color:var(--reader-border);" onclick="window.civlib.toggleHamburger()">☰</button>
              <button class="pill-btn" style="padding:6px 8px;border-radius:8px;background:#7D3C27;color:#FFF;border-color:var(--reader-border);font-size:10px;" onclick="window.civlib.toggleSpreadLayout()">2P</button>
            </div>
            <button class="pill-btn" style="padding:8px 10px;border-radius:10px;background:var(--reader-bg);color:var(--reader-ink);border-color:var(--reader-border);" onclick="window.civlib.turnPage(-2)">◀</button>
            ${hamburgerDropdownHTML}
          </div>
        ` : ''}

        <!-- Center 2-Page Open Book Spread -->
        <div style="flex:1;min-width:0;height:100%;padding:8px;display:flex;gap:6px;">
          ${renderSinglePageSheet(pageData, isPage1Cover)}
          ${rightPageData ? renderSinglePageSheet(rightPageData, false) : ''}
        </div>

        ${ui.readerShowChrome ? `
          <!-- Right Vertical Option Bar -->
          <div style="width:60px;background:var(--reader-card);color:var(--reader-ink);border-left:1px solid var(--reader-border);padding:10px 6px;display:flex;flex-direction:column;justify-content:space-between;align-items:center;flex-shrink:0;">
            <div class="mono-text" style="font-size:10px;font-weight:700;text-align:center;background:var(--reader-bg);padding:5px 4px;border-radius:8px;border:1px solid var(--reader-border);width:100%;">
              ${pageNum}${rightPageData ? '–' + rightPageData.pageNumber : ''}<br/>/${book.totalPages}
            </div>
            <div style="display:flex;flex-direction:column;gap:5px;width:100%;">
              ${[
                ['PARCHMENT', 'Book'],
                ['CRISP', 'Crisp'],
                ['BLUEPRINT_DARK', 'Night']
              ].map(([tKey, tLabel]) => `
                <button class="pill-btn" style="width:100%;padding:6px 2px;border-radius:8px;font-size:10px;background:${prefs.paperTheme === tKey ? '#7D3C27' : 'var(--reader-bg)'};color:${prefs.paperTheme === tKey ? '#FFF' : 'var(--reader-ink)'};border-color:var(--reader-border);" onclick="window.civlib.setPaperTheme('${tKey}')">${tLabel}</button>
              `).join('')}
              <button class="pill-btn" style="width:100%;padding:6px 2px;border-radius:8px;font-size:10px;background:${isTrimmed ? '#7D3C27' : 'var(--reader-bg)'};color:${isTrimmed ? '#FFF' : 'var(--reader-ink)'};border-color:var(--reader-border);" onclick="window.civlib.toggleTrimMargins()">Trim</button>
            </div>
            <button class="pill-btn" style="padding:8px 10px;border-radius:10px;background:var(--reader-bg);color:var(--reader-ink);border-color:var(--reader-border);" onclick="window.civlib.turnPage(2)">▶</button>
          </div>
        ` : ''}

        ${ui.readerShowThumbnails ? renderThumbnailsModalHTML(book, pageNum) : ''}
      </div>
    `;
  }

  // --- 14. VISUAL BOOK PAGES PREVIEW SHEET ("Tap any page to flip there") ---
  function renderThumbnailsModalHTML(book, currentPage) {
    const previewCount = Math.min(book.totalPages, 60);
    const pages = [];
    for (let p = 1; p <= previewCount; p++) {
      pages.push(getPageDataForBook(book, p));
    }

    return `
      <div class="modal-backdrop" onclick="window.civlib.closeThumbnailsSheet()">
        <div class="modal-card" style="max-width:560px;" onclick="event.stopPropagation()">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
            <div>
              <h2 class="serif-title" style="font-size:20px;">Book Pages (${book.totalPages})</h2>
              <div style="font-size:11.5px;color:var(--sand-cream-muted);">Tap any page preview to flip there</div>
            </div>
            <button class="pill-btn pill-cream" onclick="window.civlib.closeThumbnailsSheet()">Close</button>
          </div>

          <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:10px;max-height:62vh;overflow-y:auto;padding-right:4px;">
            ${pages.map((p) => `
              <div onclick="window.civlib.selectThumbnailPage(${p.pageNumber})" style="background:#F4ECD8;color:#251C15;border:${p.pageNumber === currentPage ? '2.5px solid #C89B3C' : '1px solid #D5C8AE'};border-radius:8px;padding:7px;aspect-ratio:0.74;display:flex;flex-direction:column;justify-content:space-between;cursor:pointer;overflow:hidden;">
                <div>
                  <div class="mono-text" style="font-size:7.5px;font-weight:700;color:#7D3C27;border-bottom:0.5px solid #D5C8AE;padding-bottom:2px;margin-bottom:3px;">
                    ${escapeHtml(book.courseCode)} · P.${p.pageNumber}
                  </div>
                  <div style="font-weight:700;font-size:8.5px;line-height:1.15;margin-bottom:3px;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;">
                    ${escapeHtml(p.title)}
                  </div>
                  <div class="lora-text" style="font-size:7px;line-height:1.25;color:#4A3D36;display:-webkit-box;-webkit-line-clamp:6;-webkit-box-orient:vertical;overflow:hidden;">
                    ${escapeHtml(p.body)}
                  </div>
                </div>
                <div class="mono-text" style="font-size:8px;font-weight:700;text-align:center;background:rgba(28,21,18,0.08);border-radius:4px;padding:2px 0;">
                  Page ${p.pageNumber}
                </div>
              </div>
            `).join('')}
          </div>
        </div>
      </div>
    `;
  }

  // --- 15. FULL-SCREEN AI STUDY ASSISTANT WINDOW ---
  function renderAiAssistantHTML(book) {
    const curPage = ui.readerPageNumber || book.currentPage || 1;
    const subj = SUBJECTS[book.subjectKey] || SUBJECTS.STRUCTURAL;
    const quickPrompts = [
      `Explain Page ${curPage} simply`,
      `Find key formulas in this book`,
      `Where is ${subj.short} design covered?`,
      `Summarize Page ${curPage} for my study notes`
    ];

    return `
      <div class="fullscreen-overlay">
        <!-- Top Header -->
        <div style="background:var(--espresso-card);padding: calc(12px + var(--safe-top)) 14px 12px;display:flex;align-items:center;gap:10px;border-bottom:1px solid rgba(235,224,204,0.14);">
          <button class="pill-btn pill-card" onclick="window.civlib.closeAiAssistant()">← Back</button>
          <div style="flex:1;min-width:0;">
            <div class="serif-title" style="font-size:17px;font-weight:700;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">
              ✨ AI Book Assistant · Page ${curPage}
            </div>
            <div style="font-size:11.5px;color:var(--sand-cream-muted);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">
              ${escapeHtml(book.courseCode)} · ${escapeHtml(book.title)}
            </div>
          </div>
        </div>

        <!-- Flush Quick Prompt Pills (Border & Fill 100% Matched) -->
        <div class="chip-row" style="padding:10px 14px;border-bottom:1px solid rgba(235,224,204,0.12);flex-shrink:0;">
          ${quickPrompts.map((pr) => `
            <button class="pill-btn pill-sage" onclick="window.civlib.sendAiPrompt('${escapeJs(pr)}')">${escapeHtml(pr)}</button>
          `).join('')}
        </div>

        <!-- Chat Messages -->
        <div id="ai-chat-scroll" style="flex:1;overflow-y:auto;padding:14px 16px;display:flex;flex-direction:column;gap:12px;">
          ${ui.aiMessages.map((m, idx) => `
            <div style="display:flex;flex-direction:column;align-items:${m.isUser ? 'flex-end' : 'flex-start'};">
              <div style="max-width:${m.isUser ? '84%' : '95%'};background:${m.isUser ? 'var(--sand-cream)' : 'var(--espresso-card)'};color:${m.isUser ? 'var(--espresso-ink)' : 'var(--sand-cream-light)'};border:1px solid rgba(235,224,204,0.18);border-radius:18px;padding:12px 14px;">
                ${!m.isUser ? `<div class="mono-text" style="font-size:10px;font-weight:700;color:var(--sand-cream);margin-bottom:6px;">✨ CIVLIB AI ASSISTANT</div>` : ''}
                <div class="${m.isUser ? '' : 'lora-text'}" style="font-size:13.5px;line-height:1.55;white-space:pre-wrap;">${escapeHtml(m.text)}</div>

                ${!m.isUser && ((m.suggestedPages && m.suggestedPages.length) || m.noteSummary) ? `
                  <div style="border-top:1px solid rgba(235,224,204,0.16);margin-top:10px;padding-top:10px;display:flex;flex-direction:column;gap:8px;">
                    ${m.suggestedPages && m.suggestedPages.length ? `
                      <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        ${m.suggestedPages.slice(0, 3).map((pNum) => `
                          <button class="pill-btn pill-cream" onclick="window.civlib.jumpToPageFromAi('${book.id}', ${pNum})">
                            📖 Open Page ${pNum} →
                          </button>
                        `).join('')}
                      </div>
                    ` : ''}
                    ${m.noteSummary ? `
                      <button class="pill-btn ${m.noteSavedForPage ? 'pill-sage' : 'pill-espresso'}" style="width:100%;border-radius:10px;padding:9px 12px;justify-content:flex-start;" onclick="window.civlib.saveNoteFromAi(${idx}, '${book.id}', ${m.suggestedPages[0] || curPage})">
                        ${m.noteSavedForPage ? `✓ Saved to your Study Notes for Page ${m.noteSavedForPage}` : `✎ Want me to note this for you on Page ${m.suggestedPages[0] || curPage}?`}
                      </button>
                    ` : ''}
                  </div>
                ` : ''}
              </div>
            </div>
          `).join('')}

          ${ui.aiIsThinking ? `
            <div style="background:var(--espresso-card);border-radius:16px;padding:12px 14px;font-size:12.5px;color:var(--sand-cream-muted);">
              Scanning book pages &amp; generating engineering explanation…
            </div>
          ` : ''}
        </div>

        <!-- Bottom Input Bar -->
        <form onsubmit="event.preventDefault();window.civlib.submitAiInput();" style="background:var(--espresso-card);padding:10px 14px calc(12px + var(--safe-bottom));display:flex;gap:8px;border-top:1px solid rgba(235,224,204,0.14);">
          <input
            id="ai-input-box"
            type="text"
            placeholder="Ask AI to find a topic in this book or explain Page ${curPage}…"
            style="flex:1;padding:12px 16px;border-radius:999px;border:1px solid rgba(235,224,204,0.28);background:var(--espresso-bg);color:var(--sand-cream-light);font-size:13.5px;outline:none;"
          />
          <button type="submit" class="pill-btn pill-cream" style="padding:12px 18px;">Send</button>
        </form>
      </div>
    `;
  }

  // --- 16. 4-STEP MULTI-SELECT STUDY SURVEY MODAL (With e.g. Heart Garcia) ---
  function renderSurveyModalHTML() {
    const p = state.profile;
    const step = ui.surveyStep;

    let stepBody = '';
    if (step === 0) {
      stepBody = `
        <h2 class="serif-title" style="font-size:22px;color:var(--espresso-ink);margin-bottom:6px;">1. Your Name &amp; Engineering Course</h2>
        <p class="lora-text" style="font-size:13px;color:var(--espresso-ink-muted);margin-bottom:14px;">
          No registration needed. Your name, course, reading progress, and notes stay saved automatically in your device cache.
        </p>
        <label style="font-size:12px;font-weight:700;color:var(--espresso-ink);display:block;margin-bottom:6px;">
          Your Name (e.g. Heart Garcia)
        </label>
        <input
          type="text"
          value="${escapeHtml(p.displayName)}"
          placeholder="e.g. Heart Garcia"
          oninput="window.civlib.setSurveyName(this.value)"
          style="width:100%;padding:12px 14px;border-radius:14px;border:1.5px solid var(--espresso-ink);background:#FFF;color:var(--espresso-ink);font-size:14px;font-weight:600;margin-bottom:14px;"
        />
        <div style="font-size:12.5px;font-weight:700;color:var(--espresso-ink);margin-bottom:8px;">
          What is your Course / Engineering Program?
        </div>
        <div style="display:flex;flex-direction:column;gap:7px;">
          ${COURSE_PROGRAMS.map((prog) => {
            const sel = p.courseProgram === prog;
            return `
              <div onclick="window.civlib.setSurveyCourse('${escapeJs(prog)}')" style="padding:11px 14px;border-radius:12px;background:${sel ? 'var(--espresso-ink)' : 'rgba(255,255,255,0.78)'};color:${sel ? 'var(--sand-cream-light)' : 'var(--espresso-ink)'};border:1.5px solid ${sel ? 'var(--espresso-ink)' : 'rgba(28,21,18,0.18)'};font-size:12.5px;font-weight:700;cursor:pointer;display:flex;justify-content:space-between;">
                <span>${escapeHtml(prog)}</span><span>${sel ? '✓' : ''}</span>
              </div>
            `;
          }).join('')}
        </div>
      `;
    } else if (step === 1) {
      const years = [
        [1, '1st Year · Freshman', 'Engineering Math, Calculus, Physics, Statics & Surveying'],
        [2, '2nd Year · Sophomore', 'Strength of Materials, Fluid Mechanics, Hydraulics & Allied Eng.'],
        [3, '3rd Year · Junior', 'Structural Theory, Reinforced Concrete, Steel & Soil Mechanics'],
        [4, '4th Year · Senior & Board Review', 'Construction Management, Environmental, Capstone & Licensure'],
        [5, 'Masters / Graduate Engineering (MS / MEng)', 'Advanced Structural Dynamics, Finite Elements (FEM), Plates & Shells']
      ];
      stepBody = `
        <h2 class="serif-title" style="font-size:22px;color:var(--espresso-ink);margin-bottom:6px;">2. What Year or Level Are You In?</h2>
        <p class="lora-text" style="font-size:13px;color:var(--espresso-ink-muted);margin-bottom:14px;">
          We tailor your chronological textbook roadmap around your current level.
        </p>
        <div style="display:flex;flex-direction:column;gap:8px;">
          ${years.map(([yr, title, sub]) => {
            const sel = p.curriculumYear === yr;
            return `
              <div onclick="window.civlib.setSurveyYear(${yr})" style="padding:12px 14px;border-radius:14px;background:${sel ? 'var(--espresso-ink)' : 'rgba(255,255,255,0.78)'};color:${sel ? 'var(--sand-cream-light)' : 'var(--espresso-ink)'};border:1.5px solid ${sel ? 'var(--espresso-ink)' : 'rgba(28,21,18,0.18)'};cursor:pointer;">
                <div style="font-size:13.5px;font-weight:700;">${escapeHtml(title)} ${sel ? '✓' : ''}</div>
                <div style="font-size:11.5px;opacity:0.82;margin-top:2px;">${escapeHtml(sub)}</div>
              </div>
            `;
          }).join('')}
        </div>
      `;
    } else if (step === 2) {
      stepBody = `
        <h2 class="serif-title" style="font-size:22px;color:var(--espresso-ink);margin-bottom:4px;">3. Choose Your Course Focuses</h2>
        <p class="lora-text" style="font-size:12.5px;color:var(--espresso-ink-muted);margin-bottom:12px;">
          Tap as many subjects as you want (Civil, Math/Physics, Allied &amp; Masters).
        </p>
        <div style="display:grid;grid-template-columns:repeat(2,1fr);gap:8px;max-height:46vh;overflow-y:auto;">
          ${Object.values(SUBJECTS).map((s) => {
            const sel = (p.selectedSubjectKeys || []).includes(s.key);
            return `
              <div onclick="window.civlib.toggleSurveySubject('${s.key}')" style="padding:10px 11px;border-radius:13px;background:${sel ? 'var(--espresso-ink)' : 'rgba(255,255,255,0.78)'};color:${sel ? 'var(--sand-cream-light)' : 'var(--espresso-ink)'};border:1.5px solid ${sel ? 'var(--espresso-ink)' : 'rgba(28,21,18,0.18)'};cursor:pointer;">
                <div style="font-size:10px;font-weight:700;color:${sel ? 'var(--sand-cream)' : s.color};">${escapeHtml(s.code)} · ${s.year === 5 ? 'MS' : 'Y' + s.year} ${sel ? '✓' : ''}</div>
                <div style="font-size:12.5px;font-weight:700;margin-top:2px;">${escapeHtml(s.short)}</div>
              </div>
            `;
          }).join('')}
        </div>
      `;
    } else {
      stepBody = `
        <h2 class="serif-title" style="font-size:22px;color:var(--espresso-ink);margin-bottom:4px;">4. Your Primary Study Goals</h2>
        <p class="lora-text" style="font-size:12.5px;color:var(--espresso-ink-muted);margin-bottom:12px;">
          Choose one or more study goals to rank your recommended books.
        </p>
        <div style="display:flex;flex-direction:column;gap:8px;max-height:46vh;overflow-y:auto;">
          ${STUDY_GOALS.map((g) => {
            const sel = (p.selectedGoals || []).includes(g.title);
            return `
              <div onclick="window.civlib.toggleSurveyGoal('${escapeJs(g.title)}')" style="padding:11px 14px;border-radius:14px;background:${sel ? 'var(--espresso-ink)' : 'rgba(255,255,255,0.78)'};color:${sel ? 'var(--sand-cream-light)' : 'var(--espresso-ink)'};border:1.5px solid ${sel ? 'var(--espresso-ink)' : 'rgba(28,21,18,0.18)'};cursor:pointer;">
                <div style="font-size:10px;font-weight:700;opacity:0.8;">${escapeHtml(g.badge)}</div>
                <div style="font-size:13.5px;font-weight:700;">${escapeHtml(g.title)} ${sel ? '✓' : ''}</div>
                <div style="font-size:11.5px;opacity:0.82;margin-top:2px;">${escapeHtml(g.sub)}</div>
              </div>
            `;
          }).join('')}
        </div>
      `;
    }

    return `
      <div class="fullscreen-overlay" style="overflow-y:auto;padding: calc(18px + var(--safe-top)) 18px calc(24px + var(--safe-bottom));align-items:center;justify-content:center;">
        <div style="width:100%;max-width:440px;">
          <div style="display:flex;align-items:center;justify-content:center;gap:12px;margin-bottom:14px;">
            <img src="./icons/icon-512.jpg" alt="CivLib" style="width:44px;height:44px;border-radius:50%;border:2px solid var(--sage-olive);object-fit:cover;" />
            <div>
              <div class="serif-title" style="font-size:21px;font-weight:700;">CivLib Study Profile</div>
              <div style="font-size:11.5px;color:var(--sand-cream-muted);">Always-on offline study session · Question ${step + 1} of 4</div>
            </div>
          </div>

          <div style="background:var(--sand-cream);color:var(--espresso-ink);border-radius:24px;padding:22px 20px;box-shadow:0 12px 32px rgba(0,0,0,0.45);">
            ${stepBody}
            <div style="display:flex;justify-content:space-between;align-items:center;margin-top:18px;gap:10px;">
              ${step > 0 ? `
                <button class="pill-btn" style="background:rgba(28,21,18,0.12);color:var(--espresso-ink);padding:11px 18px;" onclick="window.civlib.prevSurveyStep()">← Back</button>
              ` : state.onboardingCompleted ? `
                <button class="pill-btn" style="background:rgba(28,21,18,0.12);color:var(--espresso-ink);padding:11px 18px;" onclick="window.civlib.closeSurvey()">Cancel</button>
              ` : `<div></div>`}
              <button class="pill-btn pill-espresso" style="padding:12px 22px;font-size:13.5px;" onclick="window.civlib.nextSurveyStep()">
                ${step < 3 ? 'Next →' : 'Recommend ★'}
              </button>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // --- 17. PROFILE SETTINGS MODAL ---
  function renderSettingsModalHTML() {
    const prefs = state.preferences;
    return `
      <div class="modal-backdrop" onclick="window.civlib.closeSettings()">
        <div class="modal-card" onclick="event.stopPropagation()">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
            <div>
              <h2 class="serif-title" style="font-size:21px;">App &amp; Reader Settings</h2>
              <div style="font-size:11.5px;color:var(--sand-cream-muted);">Appearance, reading defaults, auto-sync &amp; AI Assistant</div>
            </div>
            <button class="pill-btn pill-cream" onclick="window.civlib.closeSettings()">Done</button>
          </div>

          <div style="font-size:11px;font-weight:700;color:var(--sand-cream);letter-spacing:0.8px;margin:14px 0 8px;">APPEARANCE &amp; DEFAULT PAPER THEME</div>
          <div style="display:flex;gap:8px;margin-bottom:14px;">
            ${[
              ['PARCHMENT', 'Book Parchment'],
              ['CRISP', 'Crisp White'],
              ['BLUEPRINT_DARK', 'Blueprint Night']
            ].map(([k, label]) => `
              <button class="pill-btn ${prefs.paperTheme === k ? 'pill-cream' : 'pill-card'}" style="flex:1;padding:10px 6px;border-radius:12px;" onclick="window.civlib.setPaperTheme('${k}')">${label}</button>
            `).join('')}
          </div>

          <div style="font-size:11px;font-weight:700;color:var(--sand-cream);letter-spacing:0.8px;margin:14px 0 8px;">BOOK READER DEFAULTS</div>
          <div onclick="window.civlib.togglePref('resetToPortraitTrimmedOnOpen')" style="background:var(--espresso-bg);padding:12px 14px;border-radius:12px;margin-bottom:8px;display:flex;justify-content:space-between;align-items:center;cursor:pointer;">
            <div>
              <div style="font-size:13px;font-weight:700;">Reset to Portrait Trimmed on Book Open</div>
              <div style="font-size:11px;color:var(--sand-cream-muted);">Always start books in 1-Page Portrait with margins trimmed</div>
            </div>
            <span class="pill-btn ${prefs.resetToPortraitTrimmedOnOpen ? 'pill-cream' : 'pill-card'}">${prefs.resetToPortraitTrimmedOnOpen ? 'ON' : 'OFF'}</span>
          </div>

          <div onclick="window.civlib.togglePref('cropPdfMargins')" style="background:var(--espresso-bg);padding:12px 14px;border-radius:12px;margin-bottom:8px;display:flex;justify-content:space-between;align-items:center;cursor:pointer;">
            <div>
              <div style="font-size:13px;font-weight:700;">Trim PDF Margins by Default</div>
              <div style="font-size:11px;color:var(--sand-cream-muted);">Crop wide outer PDF borders for larger diagrams</div>
            </div>
            <span class="pill-btn ${prefs.cropPdfMargins ? 'pill-cream' : 'pill-card'}">${prefs.cropPdfMargins ? 'ON' : 'OFF'}</span>
          </div>

          <div onclick="window.civlib.togglePref('autoSyncOnLaunch')" style="background:var(--espresso-bg);padding:12px 14px;border-radius:12px;margin-bottom:14px;display:flex;justify-content:space-between;align-items:center;cursor:pointer;">
            <div>
              <div style="font-size:13px;font-weight:700;">Auto-Sync Catalog Every Time App Opens</div>
              <div style="font-size:11px;color:var(--sand-cream-muted);">Automatically check for updated textbooks on launch</div>
            </div>
            <span class="pill-btn ${prefs.autoSyncOnLaunch ? 'pill-cream' : 'pill-card'}">${prefs.autoSyncOnLaunch ? 'ON' : 'OFF'}</span>
          </div>

          <div style="font-size:11px;font-weight:700;color:var(--sand-cream);letter-spacing:0.8px;margin-bottom:6px;">✨ AI STUDY ASSISTANT (FREE LLM + OPTIONAL GEMINI KEY)</div>
          <input
            type="text"
            value="${escapeHtml(prefs.customGeminiApiKey || '')}"
            placeholder="Optional: AIzaSy... (Leave blank for built-in Free AI)"
            oninput="window.civlib.setGeminiKey(this.value)"
            style="width:100%;padding:11px 14px;border-radius:12px;border:1px solid rgba(235,224,204,0.28);background:var(--espresso-bg);color:var(--sand-cream-light);font-size:12.5px;margin-bottom:14px;"
          />

          <div style="display:flex;gap:8px;">
            <button class="pill-btn pill-cream" style="flex:1;padding:11px;" onclick="window.civlib.syncCatalog()">Sync Catalog Now</button>
            <button class="pill-btn pill-sage" style="flex:1;padding:11px;" onclick="window.civlib.openInstallGuide()">iPhone Install Guide</button>
          </div>
        </div>
      </div>
    `;
  }

  function renderIosInstallGuideModalHTML() {
    return `
      <div class="modal-backdrop" onclick="window.civlib.closeInstallGuide()">
        <div class="modal-card" onclick="event.stopPropagation()">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
            <h2 class="serif-title" style="font-size:21px;">Install CivLib on iPhone / iPad</h2>
            <button class="pill-btn pill-cream" onclick="window.civlib.closeInstallGuide()">Got It</button>
          </div>
          <p class="lora-text" style="font-size:13px;color:var(--sand-cream-muted);margin-bottom:14px;">
            CivLib runs as a full-screen native-like iOS app with offline caching and never expires:
          </p>
          <div style="display:flex;flex-direction:column;gap:10px;font-size:13px;">
            <div style="background:var(--espresso-bg);padding:12px 14px;border-radius:12px;">
              <strong>Step 1:</strong> Open this page in <strong>Safari</strong> on your iPhone or iPad.
            </div>
            <div style="background:var(--espresso-bg);padding:12px 14px;border-radius:12px;">
              <strong>Step 2:</strong> Tap the <strong>Share</strong> button (the square with an upward arrow at the bottom of Safari).
            </div>
            <div style="background:var(--espresso-bg);padding:12px 14px;border-radius:12px;">
              <strong>Step 3:</strong> Scroll down and tap <strong>"Add to Home Screen"</strong> → tap <strong>Add</strong>.
            </div>
            <div style="background:var(--espresso-bg);padding:12px 14px;border-radius:12px;">
              <strong>Step 4:</strong> Launch <strong>CivLib</strong> from your Home Screen! Your profile, survey, notes, and books stay saved in your iPhone cache.
            </div>
          </div>
        </div>
      </div>
    `;
  }

  function renderBottomNavHTML() {
    const tabs = [
      { id: 'profile', label: 'Profile', icon: '👤' },
      { id: 'library', label: 'Library', icon: '📚' },
      { id: 'bookmarks', label: 'Bookmarks', icon: '🔖' },
      { id: 'discover', label: 'Home', icon: '🧭' }
    ];
    return `
      <nav class="bottom-nav">
        ${tabs.map((t) => `
          <div class="nav-item ${ui.activeTab === t.id ? 'active' : ''}" onclick="window.civlib.switchTab('${t.id}')">
            <span style="font-size:17px;">${t.icon}</span>
            <span>${t.label}</span>
          </div>
        `).join('')}
      </nav>
    `;
  }

  // --- 18. REAL PDF.JS CANVAS RENDERER FOR IMPORTED LOCAL PDFS ---
  async function renderPdfCanvasIfAvailable(book, pageNum) {
    const blobEntry = ui.localPdfBlobs[book.id];
    if (!blobEntry || !blobEntry.pdfDoc) return;
    try {
      const page = await blobEntry.pdfDoc.getPage(pageNum);
      const canvas = document.getElementById(`pdf-canvas-p${pageNum}`);
      if (!canvas) return;
      canvas.style.display = 'block';
      const viewport = page.getViewport({ scale: 1.5 });
      canvas.width = viewport.width;
      canvas.height = viewport.height;
      const ctx = canvas.getContext('2d');
      await page.render({ canvasContext: ctx, viewport: viewport }).promise;
    } catch (_) {}
  }

  function setupPdfFilePicker() {
    const picker = document.getElementById('pdf-file-picker');
    if (!picker) return;
    picker.addEventListener('change', async (e) => {
      const file = e.target.files && e.target.files[0];
      if (!file) return;
      showToast(`Importing "${file.name}"…`);
      const cleanTitle = file.name.replace(/\.pdf$/i, '').replace(/[_-]+/g, ' ');
      const subjKey = inferSubjectKey(cleanTitle);
      const subj = SUBJECTS[subjKey] || SUBJECTS.STRUCTURAL;
      const newId = 'local_pdf_' + Date.now();
      let totalPages = 50;
      const pageTexts = {};

      if (window.pdfjsLib) {
        try {
          window.pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/pdf.worker.min.js';
          const buf = await file.arrayBuffer();
          const pdfDoc = await window.pdfjsLib.getDocument({ data: buf }).promise;
          totalPages = pdfDoc.numPages;
          const maxScan = Math.min(totalPages, 25);
          for (let p = 1; p <= maxScan; p++) {
            const pg = await pdfDoc.getPage(p);
            const tc = await pg.getTextContent();
            pageTexts[p] = tc.items.map((it) => it.str).join(' ');
          }
          ui.localPdfBlobs[newId] = { pdfDoc, numPages: totalPages, pageTexts };
        } catch (_) {}
      }

      const newBook = {
        id: newId,
        title: cleanTitle,
        author: 'Imported Local PDF',
        subjectKey: subjKey,
        courseCode: subj.code,
        year: new Date().getFullYear(),
        totalPages: totalPages,
        isStarter: false,
        coverUrl: './icons/cover-structural.jpg',
        description: `Imported local PDF textbook (${totalPages} pages) classified under ${subj.name}.`,
        chapters: Object.keys(pageTexts).slice(0, 6).map((pNum) => ({
          page: Number(pNum),
          title: `Page ${pNum} — ${cleanTitle}`,
          body: pageTexts[pNum] || `Imported PDF Page ${pNum}`
        }))
      };
      state.extraBooks.unshift(newBook);
      if (!state.savedBookIds.includes(newId)) state.savedBookIds.unshift(newId);
      saveState();
      showToast(`Imported "${cleanTitle}" (${totalPages} pages)`);
      window.civlib.openReader(newId, 1);
    });
  }

  // --- 19. PUBLIC CONTROLLER ACTIONS ---
  window.civlib = {
    switchTab(tab) {
      ui.activeTab = tab;
      ui.detailBookId = null;
      ui.readerBookId = null;
      render();
    },
    setProfileTab(idx) {
      ui.profileSubTab = idx;
      render();
    },
    setProfileSubjectFilter(key) {
      ui.profileSubjectFilter = ui.profileSubjectFilter === key ? null : key;
      render();
    },
    setLibrarySubject(key) {
      ui.librarySubjectFilter = key;
      render();
    },
    onSearchInput(val) {
      ui.searchQuery = val;
      render();
    },
    openBookClick(bookId) {
      ui.detailBookId = bookId;
      ui.showcaseShowRealCover = false; // Always editorial cover by default in Showcase!
      render();
    },
    closeDetail() {
      ui.detailBookId = null;
      render();
    },
    toggleShowcaseRealCover() {
      ui.showcaseShowRealCover = !ui.showcaseShowRealCover;
      render();
    },
    openReader(bookId, pageNum) {
      // Reset to 1-Page Portrait Trimmed whenever opening a book if preference is enabled
      if (state.preferences.resetToPortraitTrimmedOnOpen) {
        state.preferences.readingLayout = 'SINGLE_PAGE';
        state.preferences.cropPdfMargins = true;
      }
      ui.readerBookId = bookId;
      ui.readerPageNumber = pageNum || state.readingProgress[bookId] || 1;
      ui.readerShowChrome = true;
      ui.readerShowHamburger = false;
      ui.readerShowThumbnails = false;
      state.readingProgress[bookId] = ui.readerPageNumber;
      saveState();
      render();
    },
    closeReader() {
      ui.readerBookId = null;
      ui.readerShowHamburger = false;
      render();
    },
    toggleReaderChrome() {
      ui.readerShowChrome = !ui.readerShowChrome;
      ui.readerShowHamburger = false;
      render();
    },
    toggleHamburger(force) {
      ui.readerShowHamburger = typeof force === 'boolean' ? force : !ui.readerShowHamburger;
      render();
    },
    turnPage(delta) {
      const book = getAllBooks().find((b) => b.id === ui.readerBookId);
      if (!book) return;
      ui.readerPageNumber = Math.min(book.totalPages, Math.max(1, (ui.readerPageNumber || 1) + delta));
      state.readingProgress[book.id] = ui.readerPageNumber;
      saveState();
      render();
    },
    goToPage(pNum) {
      const book = getAllBooks().find((b) => b.id === ui.readerBookId);
      if (!book) return;
      ui.readerPageNumber = Math.min(book.totalPages, Math.max(1, pNum));
      state.readingProgress[book.id] = ui.readerPageNumber;
      saveState();
      render();
    },
    setPaperTheme(themeKey) {
      state.preferences.paperTheme = themeKey;
      saveState();
      render();
    },
    toggleSpreadLayout() {
      state.preferences.readingLayout = state.preferences.readingLayout === 'TWO_PAGE_SPREAD' ? 'SINGLE_PAGE' : 'TWO_PAGE_SPREAD';
      ui.readerShowHamburger = false;
      saveState();
      render();
    },
    toggleTrimMargins() {
      state.preferences.cropPdfMargins = !state.preferences.cropPdfMargins;
      saveState();
      render();
    },
    openThumbnailsSheet() {
      ui.readerShowHamburger = false;
      ui.readerShowThumbnails = true;
      render();
    },
    closeThumbnailsSheet() {
      ui.readerShowThumbnails = false;
      render();
    },
    selectThumbnailPage(pNum) {
      ui.readerShowThumbnails = false;
      window.civlib.goToPage(pNum);
    },
    openJumpPagePrompt() {
      ui.readerShowHamburger = false;
      const book = getAllBooks().find((b) => b.id === ui.readerBookId);
      if (!book) return;
      const raw = prompt(`Jump to page number (1–${book.totalPages}):`, String(ui.readerPageNumber));
      const parsed = parseInt(raw, 10);
      if (!isNaN(parsed)) window.civlib.goToPage(parsed);
      else render();
    },
    quickBookmarkPage() {
      ui.readerShowHamburger = false;
      const book = getAllBooks().find((b) => b.id === ui.readerBookId);
      if (!book) return;
      if (!state.savedBookIds.includes(book.id)) state.savedBookIds.unshift(book.id);
      const pNum = ui.readerPageNumber || 1;
      const pData = getPageDataForBook(book, pNum);
      state.studyNotes.unshift({
        id: 'bm_' + Date.now(),
        bookId: book.id,
        bookTitle: book.title,
        pageNumber: pNum,
        chapterTitle: pData.title,
        noteText: `Bookmarked Page ${pNum} (${pData.title})`
      });
      saveState();
      showToast(`Bookmarked Page ${pNum}`);
      render();
    },
    openAddNotePrompt() {
      ui.readerShowHamburger = false;
      const book = getAllBooks().find((b) => b.id === ui.readerBookId);
      if (!book) return;
      const pNum = ui.readerPageNumber || 1;
      const note = prompt(`Add Study Note for Page ${pNum} of "${book.title}":`, '');
      if (note && note.trim()) {
        state.studyNotes.unshift({
          id: 'note_' + Date.now(),
          bookId: book.id,
          bookTitle: book.title,
          pageNumber: pNum,
          chapterTitle: `Page ${pNum}`,
          noteText: note.trim()
        });
        saveState();
        showToast(`Study note saved for Page ${pNum}`);
      }
      render();
    },
    openAiFromReader() {
      ui.readerShowHamburger = false;
      window.civlib.openAiForBook(ui.readerBookId);
    },
    openAiForBook(bookId) {
      const book = getAllBooks().find((b) => b.id === bookId) || STARTER_BOOKS[0];
      const curP = ui.readerPageNumber || book.currentPage || 1;
      ui.aiAssistantBookId = book.id;
      ui.aiMessages = [
        {
          id: 'welcome',
          isUser: false,
          text: `Hi! I'm your AI Study Assistant for "${book.title}" (${book.courseCode}).\n\nAsk me:\n• "Where in this book does it cover [topic/formula]?"\n• "Explain Page ${curP} simply"\n• "Summarize key formulas on this page"`,
          suggestedPages: [curP]
        }
      ];
      render();
    },
    closeAiAssistant() {
      ui.aiAssistantBookId = null;
      render();
    },
    sendAiPrompt(text) {
      const book = getAllBooks().find((b) => b.id === ui.aiAssistantBookId) || STARTER_BOOKS[0];
      askAiAssistant(book, text);
    },
    submitAiInput() {
      const box = document.getElementById('ai-input-box');
      if (!box || !box.value.trim()) return;
      const val = box.value.trim();
      box.value = '';
      window.civlib.sendAiPrompt(val);
    },
    jumpToPageFromAi(bookId, pageNum) {
      ui.aiAssistantBookId = null;
      window.civlib.openReader(bookId, pageNum);
    },
    saveNoteFromAi(msgIdx, bookId, pageNum) {
      const msg = ui.aiMessages[msgIdx];
      const book = getAllBooks().find((b) => b.id === bookId) || STARTER_BOOKS[0];
      if (!msg || msg.noteSavedForPage) return;
      state.studyNotes.unshift({
        id: 'ainote_' + Date.now(),
        bookId: book.id,
        bookTitle: book.title,
        pageNumber: pageNum,
        chapterTitle: `AI Study Summary · Page ${pageNum}`,
        noteText: msg.noteSummary || msg.text.slice(0, 320)
      });
      msg.noteSavedForPage = pageNum;
      saveState();
      showToast(`Saved AI note for Page ${pageNum}`);
      render();
    },
    toggleSaved(bookId) {
      const idx = state.savedBookIds.indexOf(bookId);
      if (idx >= 0) {
        state.savedBookIds.splice(idx, 1);
        showToast('Removed from Saved Shelf');
      } else {
        state.savedBookIds.unshift(bookId);
        showToast('Saved to your personal shelf');
      }
      saveState();
      render();
    },
    toggleCompleted(bookId) {
      const idx = state.completedBookIds.indexOf(bookId);
      if (idx >= 0) {
        state.completedBookIds.splice(idx, 1);
        showToast('Marked in progress');
      } else {
        state.completedBookIds.push(bookId);
        showToast('Marked textbook completed ✓');
      }
      saveState();
      render();
    },
    deleteNote(noteId) {
      state.studyNotes = (state.studyNotes || []).filter((n) => n.id !== noteId);
      saveState();
      showToast('Study note deleted');
      render();
    },
    openSurvey() {
      ui.surveyStep = 0;
      ui.showSurveyModal = true;
      render();
    },
    closeSurvey() {
      ui.showSurveyModal = false;
      render();
    },
    setSurveyName(name) {
      state.profile.displayName = name;
    },
    setSurveyCourse(prog) {
      state.profile.courseProgram = prog;
      if (prog.includes('Masters')) {
        state.profile.curriculumYear = 5;
        state.profile.selectedSubjectKeys = ['MASTERS_GRADUATE', 'STRUCTURAL', 'GEOTECHNICAL'];
      } else if (prog.includes('Mechanical')) {
        state.profile.selectedSubjectKeys = ['ALLIED_ENGINEERING', 'MATH_PHYSICS', 'MECHANICS'];
      } else if (prog.includes('Math')) {
        state.profile.selectedSubjectKeys = ['MATH_PHYSICS', 'MECHANICS'];
      }
      render();
    },
    setSurveyYear(yr) {
      state.profile.curriculumYear = yr;
      render();
    },
    toggleSurveySubject(key) {
      const list = state.profile.selectedSubjectKeys || [];
      const idx = list.indexOf(key);
      if (idx >= 0 && list.length > 1) list.splice(idx, 1);
      else if (idx < 0) list.push(key);
      state.profile.selectedSubjectKeys = list;
      render();
    },
    toggleSurveyGoal(goalTitle) {
      const list = state.profile.selectedGoals || [];
      const idx = list.indexOf(goalTitle);
      if (idx >= 0 && list.length > 1) list.splice(idx, 1);
      else if (idx < 0) list.push(goalTitle);
      state.profile.selectedGoals = list;
      render();
    },
    prevSurveyStep() {
      ui.surveyStep = Math.max(0, ui.surveyStep - 1);
      render();
    },
    nextSurveyStep() {
      if (ui.surveyStep < 3) {
        ui.surveyStep++;
        render();
        return;
      }
      // Finalize Survey & Recommend
      const p = state.profile;
      p.displayName = (p.displayName || '').trim() || 'Heart Garcia';
      const courseShort = p.courseProgram.includes('Masters') ? 'MSCE' : p.courseProgram.includes('Mechanical') ? 'Allied Eng' : 'BSCE';
      const subjNames = (p.selectedSubjectKeys || ['MECHANICS']).slice(0, 2).map((k) => (SUBJECTS[k] || SUBJECTS.MECHANICS).short).join(', ');
      p.studyMantra = `${courseShort} · ${subjNames}`;
      state.onboardingCompleted = true;
      ui.showSurveyModal = false;
      ui.activeTab = 'profile';
      ui.profileSubTab = 1;
      ui.profileSubjectFilter = null;
      saveState();
      showToast(`Recommendations updated for ${p.displayName}`);
      render();
    },
    openSettings() {
      ui.showSettingsModal = true;
      render();
    },
    closeSettings() {
      ui.showSettingsModal = false;
      saveState();
      render();
    },
    togglePref(key) {
      state.preferences[key] = !state.preferences[key];
      saveState();
      render();
    },
    setGeminiKey(val) {
      state.preferences.customGeminiApiKey = val.trim();
      saveState();
    },
    openInstallGuide() {
      ui.showSettingsModal = false;
      ui.showInstallGuideModal = true;
      render();
    },
    closeInstallGuide() {
      ui.showInstallGuideModal = false;
      render();
    },
    syncCatalog() {
      syncOpenAccessCatalog(false);
    },
    triggerPdfImport() {
      const picker = document.getElementById('pdf-file-picker');
      if (picker) picker.click();
    }
  };

  function escapeHtml(str) {
    return String(str == null ? '' : str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }
  function escapeJs(str) {
    return String(str == null ? '' : str).replace(/\\/g, '\\\\').replace(/'/g, "\\'");
  }

  // Initialize on DOM load
  window.addEventListener('DOMContentLoaded', () => {
    runLaunchIntro();
    setupPdfFilePicker();
    render();
    if (state.preferences.autoSyncOnLaunch) {
      syncOpenAccessCatalog(true);
    }
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.register('./sw.js').catch(() => {});
    }
  });
})();
