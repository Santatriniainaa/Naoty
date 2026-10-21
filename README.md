# Naoty — Carnet de notes universitaire

Naoty est une application de bureau dédiée à la gestion des notes universitaires. Elle permet aux administrateurs et aux professeurs d’accéder à des fonctionnalités adaptées à leurs rôles, de gérer les résultats des étudiants et de générer des bulletins de notes au format PDF. Son interface moderne et intuitive s’adapte aux préférences de chacun, avec plusieurs langues disponibles : français, anglais et malgache, ainsi qu’un choix entre le mode clair et le mode sombre.

![Java](https://img.shields.io/badge/Java-21-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue)
![Build](https://img.shields.io/badge/build-passing-brightgreen)
![Tests](https://img.shields.io/badge/tests-6%2F6-success)
![Statut](https://img.shields.io/badge/statut-livrable-success)

---

## Sommaire

1. [Aperçu](#aperçu)
2. [Prérequis](#prérequis)
3. [Démarrage rapide](#démarrage-rapide)
4. [Comptes par défaut](#comptes-par-défaut)
5. [Fonctionnalités](#fonctionnalités)
6. [Modèle de données](#modèle-de-données)
7. [Architecture](#architecture)
8. [Structure du projet](#structure-du-projet)
9. [Configuration](#configuration)
10. [Calculs métier](#calculs-métier)
11. [Bulletins PDF](#bulletins-pdf)
12. [Import / Export CSV](#import--export-csv)
13. [Internationalisation](#internationalisation)
14. [Tests](#tests)
15. [Packaging](#packaging)
16. [Crédits](#crédits)

---

## Aperçu

**Naoty** est un carnet de notes pour l'enseignement supérieur, conçu pour les
établissements LMD (Licence-Master-Doctorat). Concrètement, l'application :

- gère les **étudiants**, **professeurs**, **matières** et **notes** d'une structure universitaire ;
- propose **deux profils utilisateurs** (admin pour la gestion globale, prof pour la saisie de notes) ;
- couvre les **5 niveaux** L1, L2, L3, M1, M2 et les **10 semestres** S1 → S10 ;
- distingue les formations **Académique** et **Professionnel** (matières partageables `BOTH`) ;
- supporte trois types d'évaluation **DS / TP / Examen** avec **pondération configurable par matière** ;
- calcule automatiquement **moyennes par matière**, **moyenne semestrielle**, **rang dans la cohorte** et **mention** ;
- génère un **bulletin PDF** complet par étudiant et par semestre ;
- propose l'**import / export CSV** de toutes les entités avec preview de validation ;
- fonctionne entièrement **hors-ligne** — pas de base de données, pas de serveur.

## Prérequis

### Logiciel

| Composant | Version minimale | Recommandé | Notes |
|---|---|---|---|
| **OS** | Windows 10 / macOS 11 / Ubuntu 20.04 ou équivalent | dernière LTS | Tout OS supportant JavaFX 21 |
| **Java JDK** | 21 LTS | 21 LTS (OpenJDK ou Oracle) | Vérifier : `java --version` |
| **Maven** | — | — | **Pas besoin de l'installer** : le wrapper `./mvnw` télécharge Maven 3.9.9 au premier lancement. Non requis pour exécuter le jar |
| **Police système** | n/a | Inter, Segoe UI ou Helvetica | Fallback automatique sur Arial |

### Matériel

| Ressource | Minimum | Recommandé |
|---|---|---|
| **Processeur** | Dual-core 1,6 GHz | Quad-core 2 GHz |
| **RAM** | 512 Mo libres | 1 Go libre |
| **Disque** | 100 Mo (jar + JDK runtime + données) | 200 Mo |
| **Résolution écran** | 1024 × 720 | 1280 × 800 ou plus |
| **Carte graphique** | Toute carte supportant OpenGL 2.0+ | accélération matérielle JavaFX |
| **Réseau** | aucun | aucun — l'app fonctionne hors-ligne |

L'empreinte mémoire en cours d'utilisation tourne autour de **180-250 Mo**
(JavaFX + données chargées en cache).

## Démarrage rapide

### Lancer en mode développement

```bash
git clone git@github.com:Santatriniainaa/Naoty.git #git clone https://github.com/Santatriniainaa/Naoty.git
cd Naoty
./mvnw javafx:run
```

Au premier lancement, le dossier `data/` est créé et peuplé automatiquement
depuis `data-sample/` (4 profs, 8 étudiants, 6 matières, 16 notes pour
exploration immédiate).

### Lancer le jar livrable

```bash
./mvnw clean package
java -jar target/gradebook-0.1.0-SNAPSHOT-fat.jar
```

Le fat jar (~16 Mo) embarque toutes les dépendances et les CSV d'exemple en
fallback. Il est **portable** : copie sur tout poste avec Java 21+ →
`java -jar gradebook-0.1.0-SNAPSHOT-fat.jar` suffit.

### Commandes principales

| Commande | Effet |
|---|---|
| `./mvnw javafx:run` | Lance l'application depuis les sources |
| `./mvnw test` | Exécute les tests unitaires (JUnit 5 + AssertJ + Mockito) |
| `./mvnw clean package` | Compile + tests + génère `target/gradebook-*-fat.jar` |
| `./mvnw compile` | Compile sans tester ni packager |

> Sous Windows, utilise `mvnw.cmd` au lieu de `./mvnw`.

## Comptes par défaut

| Identifiant | Mot de passe | Rôle | Vue après login |
|---|---|---|---|
| `ADMIN` | — *(champ unique)* | Administrateur | 6 sections + Paramètres |
| `PROF-001` | — | Jean Rakoto | 2 sections + Paramètres |
| `PROF-002` | — | Marie Rabe | idem |
| `PROF-003` | — | Paul Randria | idem |
| `PROF-004` | — | Claire Andriamanjato | idem |

L'écran de login propose un **champ unique** : taper `ADMIN` (insensible à la
casse) ou un ID prof. Pas de mot de passe — adapté à un usage intranet /
poste partagé. Le code admin est configurable dans `application.properties`
(`auth.admin.code`).

## Fonctionnalités

### Côté Administrateur

| Section | Capacités |
|---|---|
| **Tableau de bord** | 4 stat cards (étudiants, profs, matières, moyenne globale) + 2 graphiques : moyenne annuelle par niveau (BarChart) + répartition des mentions (PieChart) |
| **Professeurs** | CRUD complet — ajouter / modifier / supprimer |
| **Étudiants** | CRUD + recherche live + filtres tri-axes (niveau / formation / status) |
| **Matières** | CRUD + assignation prof + poids DS/TP/EXAM par matière + filtres (niveau / semestre / formation / prof) |
| **Bulletins** | Sélection étudiant + semestre → preview live (DS / TP / EXAM / Moyenne) → génération PDF |
| **Import / Export** | Page 4 cartes : export CSV par entité + import via wizard avec preview de validation |
| **Paramètres** | Bascule thème (clair / sombre) + langue (FR / EN / MG) |

### Côté Professeur

| Section | Capacités |
|---|---|
| **Tableau de bord** | Salutation personnalisée + 3 stats (mes matières, notes saisies, étudiants concernés) |
| **Mes matières** | Liste read-only des matières assignées, avec nombre de notes saisies par matière |
| **Saisie en lot** | Tableau éditable : choisir type (DS/TP/EXAM) → saisir une note par étudiant matchant niveau+formation → upsert intelligent (création / mise à jour / suppression selon contenu) |
| **Paramètres** | Identique à l'admin |

### Transverse

- **Sidebar adaptative** — entrées différentes selon ADMIN ou PROF
- **EventBus interne** — les écrans se rafraîchissent automatiquement après une modification (table students → dashboard se met à jour)
- **Backup automatique** — chaque écriture CSV crée d'abord une copie horodatée dans `data/backup/`
- **Lecture tolérante** — une ligne CSV corrompue est loggée puis ignorée, l'application ne crashe pas
- **Validation centralisée** — un `Validator<T>` par entité, retourne un `ValidationResult` (jamais d'exception sur règle métier prévisible)

## Modèle de données

```text
User (users.csv)
├── id       "PROF-001"
├── role     PROF
├── nom
└── prenom

Student (students.csv)
├── id          "STU-001"
├── nom, prenom
├── sexe        M | F
├── niveau      L1 | L2 | L3 | M1 | M2
├── formation   ACADEMIQUE | PROFESSIONNEL
└── status      PASSANT | REDOUBLANT

Subject (subjects.csv)                    
├── id           "MATH-L1-S1"
├── nom          "Mathématiques I"        
├── coefficient  1..10                    
├── niveau       L1..M2                   
├── semestre     S1..S10                  
├── formation    ACADEMIQUE | PROFESSIONNEL | BOTH     
├── profId       → User.id                
├── weightDS     0..1                     
├── weightTP     0..1
└── weightExam   0..1

Grade (grades.csv)
├── id          "G-xxxxxx"
├── studentId   → Student.id
├── subjectId   → Subject.id
├── profId      → User.id
├── type        DS | TP | EXAM
├── value       0..20
└── date        ISO LocalDate
```

**Invariants** (vérifiés par les records et les validators) :
- `Subject.semestre.niveau() == Subject.niveau` (cohérence niveau / semestre)
- `weightDS + weightTP + weightExam > 0`
- `0 ≤ Grade.value ≤ 20`, `1 ≤ Subject.coefficient ≤ 10`
- Un étudiant a `formation = ACADEMIQUE` ou `PROFESSIONNEL` (jamais `BOTH`)

## Architecture

Couches strictes — une couche ne connaît que la couche immédiatement inférieure :

```text
view (FXML + CSS)
  ↓  FXMLLoader + ControllerFactory
controller (JavaFX)            ← adaptateur fin, aucun calcul métier
  ↓  appelle
service (logique métier pure)  ← testable sans JavaFX
  ↓  via interface
repository (interface)         ← contrat de stockage
  ↓  implémenté par
repository/csv                 ← AbstractCsvRepository + mappers
  ↓  manipule
model (records immuables)
```

### Composants transverses

| Composant | Rôle |
|---|---|
| `AppContext` | Container DI manuel — instance unique, créée dans `Main.init()` |
| `AppConfig` | Charge `application.properties` (chemins, seuils, locale) |
| `Session` | Holder mutable du rôle + User connecté |
| `AuthService` | Login champ unique : code admin ou ID prof |
| `EventBus` | Bus interne (`PropertyChangeSupport`) pour rafraîchir les vues |
| `I18n` | Helper `ResourceBundle` + `MessageFormat` |
| `Validator<T>` | Validateur composable retournant `ValidationResult` |
| `AbstractCsvRepository<T,ID>` | Cache mémoire + backup horodaté + lecture tolérante |
| `SceneRouter` | Bascule de scène login ↔ shell, applique le thème |
| `ControllerFactory` | Injecte `AppContext` dans les controllers via FXMLLoader |
| `Modal` | Helper pour ouvrir un FXML en fenêtre modale |
| `AlertUtils` | Alerts standardisés (info / error / validation / confirm) |

## Structure du projet

```text
Naoty/
├── pom.xml                            ← Maven + dépendances + plugin shade
├── README.md
├── data-sample/                       ← CSV de démo (copiés au premier run)
│   ├── users.csv
│   ├── students.csv
│   ├── subjects.csv
│   └── grades.csv
├── src/
│   ├── main/
│   │   ├── java/com/naoty/gradebook/
│   │   │   ├── Main.java              ← boot Application JavaFX
│   │   │   ├── Launcher.java          ← entry-point fat jar (non Application)
│   │   │   ├── AppContext.java        ← DI container
│   │   │   ├── auth/                  ← Session, AuthService
│   │   │   ├── config/                ← AppConfig
│   │   │   ├── controller/
│   │   │   │   ├── (Login, Shell, Placeholder, Settings, ControllerFactory)
│   │   │   │   ├── admin/             ← 10 controllers admin
│   │   │   │   └── prof/              ← 3 controllers prof
│   │   │   ├── event/                 ← EventBus
│   │   │   ├── exception/             ← DomainException + sous-classes
│   │   │   ├── model/                 ← 12 records / enums
│   │   │   ├── repository/
│   │   │   │   ├── Repository.java + 4 interfaces
│   │   │   │   └── csv/               ← AbstractCsvRepository + 4 impls
│   │   │   ├── service/               ← 9 services métier
│   │   │   ├── ui/                    ← AlertUtils, Modal, SceneRouter, I18n
│   │   │   └── validation/            ← Validator + 4 impls + ValidationResult
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── logback.xml
│   │       ├── fxml/                  ← 16 FXML (login, shell, settings, admin/*, prof/*)
│   │       ├── css/                   ← theme-light.css, theme-dark.css
│   │       ├── i18n/                  ← messages_fr / _en / _mg .properties
│   │       └── data-sample/           ← fallback classpath pour seed
│   └── test/java/                     ← StatsServiceTest (6 cas)
├── data/                              ← runtime, gitignored, créé au premier run
│   ├── users.csv, students.csv, subjects.csv, grades.csv
│   └── backup/                        ← .bak horodatés
├── bulletins/                         ← PDF générés (gitignored)
└── logs/                              ← rotation journalière Logback (gitignored)
```

## Configuration

Tout est centralisé dans [`src/main/resources/application.properties`](src/main/resources/application.properties) :

```properties
# Chemins fichiers CSV
data.dir=data
data.backup.dir=data/backup
data.file.users=users.csv
data.file.students=students.csv
data.file.subjects=subjects.csv
data.file.grades=grades.csv

# Authentification
auth.admin.code=ADMIN

# Interface
app.locale=fr-FR
app.theme=light

# Seuils de mention (sur 20)
mention.passable=10.0
mention.assezBien=12.0
mention.bien=14.0
mention.tresBien=16.0
mention.excellent=18.0
```

Le thème et la langue peuvent être basculés à chaud depuis la page
**Paramètres** (session courante uniquement — la prochaine démarrage relit
`application.properties`).

## Calculs métier

### Moyenne par matière (pondérée par type)

```
moy_matière = Σ(moyenne_type × poids_type) / Σ(poids_type présents)
```

Les types absents sont ignorés et les poids restants se **renormalisent
automatiquement**. Exemple avec poids DS=30%, TP=20%, EXAM=50%, mais
l'étudiant n'a que DS et EXAM :

```
moy = (note_DS × 0.30 + note_EXAM × 0.50) / (0.30 + 0.50)
    = (note_DS × 0.30 + note_EXAM × 0.50) / 0.80
```

### Moyenne semestrielle (pondérée par coefficient)

```
moy_semestrielle = Σ(moy_matière × coef) / Σ(coef)
   pour toute matière m avec :
     m.niveau    == student.niveau
   ∧ m.semestre  == semestre
   ∧ m.formation.matches(student.formation)
```

`Formation.BOTH` correspond aux deux formations (Académique ET Professionnel).

### Rang dans la cohorte

```
rang = position dans (étudiants de mêmes niveau + formation),
       triés par moyenne semestrielle décroissante (1 = major)
```

### Mention

Seuils configurables. Avec les valeurs par défaut :

```
INSUFFISANT < 10 ≤ PASSABLE < 12 ≤ ASSEZ_BIEN < 14 ≤ BIEN < 16 ≤ TRES_BIEN < 18 ≤ EXCELLENT
```

## Bulletins PDF

L'admin sélectionne un étudiant + un semestre dans la page **Bulletins** :

1. Aperçu live : tableau matière × DS × TP × EXAM × Moyenne par matière
2. Moyenne semestrielle + mention + rang dans la cohorte
3. Click **Générer PDF** → fichier `bulletins/bulletin-{ID}-{Semestre}.pdf`

Le PDF est généré au format A4 via **OpenPDF** et contient :

- En-tête : nom de l'étudiant, ID, status, niveau + formation, semestre
- Tableau détaillé matière / coef / DS / TP / EXAM / moyenne
- Pied : moyenne semestrielle, mention, rang
- Date d'édition

## Import / Export CSV

Accessible depuis la sidebar admin → **Import / Export**.

### Export

Cliquer "Exporter" sur une carte (Profs / Étudiants / Matières / Notes) :

1. FileChooser pour choisir la destination
2. Copie du CSV runtime vers l'emplacement choisi
3. Toast de confirmation

### Import (wizard avec preview)

Cliquer "Importer" → FileChooser → ouverture du **wizard modal** :

1. **Parse** : chaque ligne lue avec le même mapper que le repository
2. **Preview** : tableau coloré
   - Lignes vertes = valides (seront créées ou mises à jour)
   - Lignes ambres = erreurs (ignorées) avec colonne "Erreur" explicative
3. Stats en haut : `X valides · Y erreurs · Total Z`
4. Click **Importer les lignes valides** → confirmation → **upsert par ID**

> Mode upsert : `repo.deleteById(id)` puis `repo.save(entity)`. Les
> enregistrements existants avec le même ID sont remplacés. Un backup
> horodaté est créé avant chaque écriture.

L'en-tête CSV doit **correspondre exactement** à celui des fichiers
d'export (insensible à la casse). En cas de mismatch, le wizard l'indique
et bloque l'import.

## Internationalisation

Trois langues sont incluses et sélectionnables dans **Paramètres** :

| Locale | Code | Fichier |
|---|---|---|
| Français (défaut) | `fr-FR` | `i18n/messages_fr.properties` |
| English | `en-US` | `i18n/messages_en.properties` |
| Malagasy | `mg-MG` | `i18n/messages_mg.properties` |

Toute chaîne UI passe par `I18n.get("key")` côté code, ou via les attributs
FXML `text="%key"` et `promptText="%key"` résolus par le `ResourceBundle`
passé au `FXMLLoader`. Le changement de langue à chaud recharge le shell
pour que toutes les pages récupèrent le nouveau bundle.

## Tests

Framework : **JUnit 5 + AssertJ + Mockito**. Couverture ciblée sur la logique
métier critique (`StatsService`) :

```bash
./mvnw test
```

```text
StatsServiceTest
 ✓ subjectAverage_pondereParPoidsDsTpExam
 ✓ subjectAverage_renormaliseSiTypeManquant
 ✓ semestreAverage_pondereParCoefficients
 ✓ mention_seuilsCorrects
 ✓ rank_classeParCohorteNiveauPlusFormation
 ✓ formation_BOTH_matchAcademiqueEtProfessionnel

Tests run: 6, Failures: 0, Errors: 0
BUILD SUCCESS
```

## Packaging

Le projet produit un **fat jar autonome** (~16 Mo) via le plugin
`maven-shade-plugin`. Le manifest pointe sur
`com.naoty.gradebook.Launcher` — une classe distincte de `Main extends
Application`, indispensable pour démarrer JavaFX depuis un jar non-modulaire.

```bash
./mvnw clean package
java -jar target/gradebook-0.1.0-SNAPSHOT-fat.jar
```

Le jar est portable : copie sur n'importe quelle machine avec Java 21+,
double-clic ou `java -jar ...`. Les données sont écrites dans le dossier
courant (`data/`, `bulletins/`, `logs/`).

## Crédits

| Composant | Version | Usage |
|---|---|---|
| [JavaFX](https://openjfx.io/) | 21 | Framework UI desktop |
| [OpenCSV](http://opencsv.sourceforge.net/) | 5.9 | Lecture / écriture CSV robuste |
| [OpenPDF](https://github.com/LibrePDF/OpenPDF) | 2.0.3 | Génération de bulletins PDF |
| [Ikonli](https://kordamp.org/ikonli/) | 12.3.1 | Icônes vectorielles (pack Feather) |
| [SLF4J + Logback](https://logback.qos.ch/) | 2.0 / 1.5 | Logging applicatif |
| [JUnit 5](https://junit.org/junit5/) | 5.10 | Framework de tests |
| [AssertJ](https://assertj.github.io/doc/) | 3.25 | Assertions fluides |
| [Mockito](https://site.mockito.org/) | 5.11 | Mocks de repositories |

Design system inspiré de Linear / Notion / Stripe Dashboard.

---

**Version** : 0.1.0-SNAPSHOT · **Java** : 21 LTS · **JavaFX** : 21
