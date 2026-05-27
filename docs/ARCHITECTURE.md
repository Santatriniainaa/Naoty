# Architecture — Naoty Gradebook

## Couches

```
┌────────────────────────────────────────┐
│  view (FXML + CSS)                     │  ← src/main/resources/fxml/, css/
└────────────────┬───────────────────────┘
                 │ FXMLLoader + ControllerFactory
┌────────────────┴───────────────────────┐
│  controller (JavaFX)                   │  ← ne contient PAS de logique métier
│  - MainController, DashboardController │
│  - StudentController, SubjectController│
│  - GradeController                     │
└────────────────┬───────────────────────┘
                 │ appelle
┌────────────────┴───────────────────────┐
│  service (logique métier pure)         │  ← testable sans JavaFX
│  - StudentService, SubjectService      │
│  - GradeService, StatsService          │
│  - BackupService                       │
└────────────────┬───────────────────────┘
                 │ délègue à
┌────────────────┴───────────────────────┐
│  repository (interface)                │  ← contrat de stockage
│  - Repository<T,ID>                    │
│  - StudentRepository, SubjectRepo...   │
└────────────────┬───────────────────────┘
                 │ implémenté par
┌────────────────┴───────────────────────┐
│  repository/csv (impl)                 │
│  - AbstractCsvRepository<T,ID>         │
│  - CsvStudentRepository, ...           │
│  - CsvMapper<T>                        │
└────────────────┬───────────────────────┘
                 │ manipule
┌────────────────┴───────────────────────┐
│  model (record immuables)              │
│  - Student, Subject, Grade             │
│  - enums : Gender, Semester, Mention   │
└────────────────────────────────────────┘
```

## Composants transverses

- **AppContext** : container DI manuel. Une instance unique.
- **AppConfig** : charge `application.properties`. Aucun chemin/seuil ailleurs.
- **EventBus** : propagation des changements (PropertyChangeSupport).
- **I18n** : helper `ResourceBundle`. Toute chaîne UI passe par lui.
- **Validator<T>** : composable via `.and(...)`. Retourne `ValidationResult`.

## Flux d'une création d'étudiant

```
UI (form FXML)
  → StudentController.onSave()
    → StudentService.create(student)
       1. StudentValidator.validate(student) → ValidationResult
       2. si ok : StudentRepository.save(student)
          → AbstractCsvRepository.save()
             a. backup .bak horodaté
             b. réécriture du CSV complet
       3. EventBus.publish("students.changed", student)
  ← retour ValidationResult
  → si invalide : Alert d'erreur (clés i18n)
  → si valide   : DashboardController et autres écrans se rafraîchissent
                  automatiquement via subscribe("students.changed", ...)
```

## Stockage CSV — détails

- Tous les fichiers dans `data/` (configurable via `data.dir`).
- Cache mémoire chargé une seule fois au premier `findAll()`.
- Chaque `save()` ou `deleteById()` :
  1. crée un backup horodaté dans `data/backup/`
  2. réécrit le fichier complet
- Lecture tolérante : ligne corrompue → WARN + skip, pas de crash.
- En-tête CSV obligatoire.

## Pourquoi un Repository derrière interface alors qu'on n'a que CSV ?

Pas pour migrer le stockage. **Pour isoler** : la logique métier ne sait pas où
sont stockées les données, et les tests unitaires de `StatsService` peuvent
utiliser des mocks (voir `StatsServiceTest`) sans toucher au disque.
