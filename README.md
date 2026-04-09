# PMT - Project Management Tool

Application de gestion de projets composée de :

* **Frontend** : Angular
* **Backend** : Spring Boot
* **CI/CD** : GitHub Actions
* **Conteneurisation** : Docker
* **Registry** : Docker Hub

---

# Structure du projet

```text
.
├── frontend/
│   └── pmt_front/
│       ├── Dockerfile
│       └── ...
│
├── backend/
│   └── pmt/
│       ├── Dockerfile
│       └── ...
│
└── .github/
    └── workflows/
        └── ci-cd.yml
```

---

# Lancer le projet en local

## Frontend

Se placer dans le dossier :

```bash
cd frontend/pmt_front
```

Installer les dépendances :

```bash
npm install
```

Lancer l’application :

```bash
ng serve
```

Application accessible sur :

```text
http://localhost:4200
```

---

## Backend

Se placer dans le dossier :

```bash
cd backend/pmt
```

Lancer l’application :

```bash
mvn spring-boot:run
```

API accessible sur :

```text
http://localhost:8080
```

---

# Tests

## Frontend

```bash
cd frontend/pmt_front
npm test -- --watch=false --browsers=ChromeHeadless
```

## Backend

```bash
cd backend/pmt
mvn test
```

---

# Déploiement avec CI/CD

Le projet utilise **GitHub Actions** pour automatiser :

* le build
* les tests
* la création des images Docker
* le push sur Docker Hub

Le workflow se trouve ici :

```text
.github/workflows/ci-cd.yml
```

---

# Procédure de déploiement

Le déploiement se déclenche automatiquement lors d’un push sur la branche `main`.

```bash
git add .
git commit -m "deployment update"
git push origin main
```

---

# Étapes exécutées automatiquement

## Frontend

* installation des dépendances
* build Angular
* exécution des tests

```bash
npm install
npm run build
npm test
```

## Backend

* compilation Maven
* exécution des tests unitaires

```bash
mvn clean install
mvn test
```

## Docker

Création des images :

```bash
docker build -t <dockerhub-user>/pmt-front ./frontend/pmt_front
docker build -t <dockerhub-user>/pmt-back ./backend/pmt
```

Push sur Docker Hub :

```bash
docker push <dockerhub-user>/pmt-front
docker push <dockerhub-user>/pmt-back
```

---

# Secrets GitHub requis

Dans le dépôt GitHub, configurer les secrets suivants :

```text
ASTORA
THE_SECRETS_OF_THE_BLACK_ARTS
```

## Valeurs

```text
ASTORA = votre username Docker Hub
THE_SECRETS_OF_THE_BLACK_ARTS = mot de passe ou access token Docker Hub
```

Chemin :

```text
Settings > Secrets and variables > Actions
```

---

# Images Docker générées

Les images sont publiées sur Docker Hub :

```text
<dockerhub-user>/pmt-front
<dockerhub-user>/pmt-back
```

---

# Déploiement manuel depuis Docker Hub

## Pull des images

```bash
docker pull <dockerhub-user>/pmt-front
docker pull <dockerhub-user>/pmt-back
```

## Lancer les conteneurs

### Backend

```bash
docker run -p 8080:8080 <dockerhub-user>/pmt-back
```

### Frontend

```bash
docker run -p 4200:80 <dockerhub-user>/pmt-front
```

---

# Auteur

Projet réalisé dans le cadre du module DevOps / CI-CD.
