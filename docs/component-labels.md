# Components and labels

> Status: reference documentation, describes the label taxonomy as used on GitHub Issues as of 2026-09.

This repository holds no application code — it is the primary, cross-repository place to register issues for the whole Arquivo.pt service, because many changes span more than one component/repo. Issues are organized with a `Component-<Pillar>-<Name>` label per affected component, plus a small set of cross-cutting labels for triage.

## Table of Contents

- [Component pillars](#component-pillars)
  - [DataAcquisition](#dataacquisition)
  - [Replay](#replay)
  - [PageSearch](#pagesearch)
  - [ImageSearch](#imagesearch)
  - [Frontend](#frontend)
  - [ContinuousIntegration](#continuousintegration)
  - [Monitoring](#monitoring)
- [Other labels](#other-labels)
- [Filing an issue](#filing-an-issue)

---

## Component pillars

Each component label carries a short description of the technology behind it. The actual code for most components lives in a separate sibling repository (e.g. `pywb-arquivo`, `arquivo-webapp-eros`, `page-search-api`, `image-search-api`) — this repo only tracks the issue.

### DataAcquisition

Crawling and content ingest.

| Label | Technology |
|---|---|
| `Component-DataAcquisition-Heritrix` | Internet Archive Heritrix — primary, high-throughput daily crawler |
| `Component-DataAcquisition-Brozzler` | Internet Archive Brozzler — being phased out in favor of Browsertrix |
| `Component-DataAcquisition-Browsertrix` | Browsertrix — browser-based crawler, replacing Brozzler |
| `Component-DataAcquisition-AutoPatching` | Puppeteer, pywb — patches missing resources on already-crawled pages |
| `Component-DataAcquisition-ArchivePageNow` | pywb recorder — powers the public "Save Page Now" feature |
| `Component-DataAcquisition-CompletePage` | pywb recorder — tries to complete missing data on a page at replay time |
| `Component-DataAcquisition-Donations` | Converters from various formats into (w)ARC |
| `Component-DataAcquisition-CitationSaver` | Google Form + Python — saves citations from external sources on request |
| `Component-DataAcquisition-SeedsGetter` | Shell/Python scripts — discovers seed URLs via external APIs |

### Replay

Serving archived content back to users.

| Label | Technology |
|---|---|
| `Component-Replay-Wayback` | pywb |
| `Component-Replay-CDXIndexing` | Offline processing that builds the CDX/CDXJ replay indexes |
| `Component-Replay-ArcProxy` | BerkeleyDB — legacy (w)ARC lookup layer, being migrated to Redis |
| `Component-Replay-DocumentBrowser` | Tomcat webapp — serves raw (w)ARC file chunks |
| `Component-Replay-Screenshot` | Web Render, Puppeteer |
| `Component-Replay-Arquivo404` | Client-side JavaScript — suggests archived versions of dead links |

### PageSearch

Full-text search over archived pages.

| Label | Technology |
|---|---|
| `Component-PageSearch-API` | Tomcat webapp |
| `Component-PageSearch-SolrSearchEngine` | Apache Solr — new search engine, replacing NutchWAX |
| `Component-PageSearch-NutchWaxSearchEngine` | Java NutchWAX — legacy search engine |
| `Component-PageSearch-Indexing` | Hadoop v3.3.6 offline indexing pipeline |

### ImageSearch

Image search over archived pages.

| Label | Technology |
|---|---|
| `Component-ImageSearch-API` | Tomcat webapp |
| `Component-ImageSearch-SolrSearchEngine` | Apache Solr |
| `Component-ImageSearch-Indexing` | Hadoop v3 offline indexing pipeline |
| `Component-ImageSearch-SafeImageClassifier` | Offline safe/NSFW image classification |

### Frontend

Public-facing sites and apps.

| Label | Technology |
|---|---|
| `Component-Frontend-Webapp` | Node.js — main arquivo.pt search site |
| `Component-Frontend-Wordpress` | WordPress — sobre.arquivo.pt informational site |
| `Component-Frontend-Memorial` | Redirect app for domains that stopped renewing |
| `Component-Frontend-ApacheHTTPd` | Apache HTTPd reverse proxy |
| `Component-Frontend-ViagensNoTempo` | HTML/JS/CSS micro-site |
| `Component-Frontend-QuerySuggestion` | Spellchecker/autocomplete |

### ContinuousIntegration

> Note: this pillar's labels are inconsistently spelled on GitHub — most use `Component-ContinuosIntegration-*` (missing the second "u"), except `Component-ContinuousIntegration-ArtifactRepository`, which is spelled correctly. Match the exact label when searching/filtering.

| Label | Technology |
|---|---|
| `Component-ContinuosIntegration-ConfigurationManage` | Ansible |
| `Component-ContinuosIntegration-CodeRepository` | GitHub |
| `Component-ContinuosIntegration-RoutineScheduler` | Jenkins |
| `Component-ContinuosIntegration-CodeQA` | SonarCloud & Sauce Labs |
| `Component-ContinuousIntegration-ArtifactRepository` | builds.arquivo.pt |

### Monitoring

| Label | Technology |
|---|---|
| `Component-Monitoring-Statistics` | Awstats, Google Analytics, in-house LogAnalyzer |
| `Component-Monitoring-Alarms` | Icinga & Uptime Robot |
| `Component-Monitoring-Security` | Apache QoS |
| `Component-Monitoring-Capacity` | Cacti |

---

## Other labels

Cross-cutting labels used alongside (not instead of) a `Component-*` label:

| Label | Meaning |
|---|---|
| `Type-Bug` | Something is broken |
| `Type-Enhancement` | Improvement to existing behavior |
| `Type-Task` | Work item that isn't a bug or a user-facing enhancement |
| `Impact-Low` / `Impact-Medium` / `Impact-High` | Severity/priority of the issue |
| `objectives` | A task tied to the service's or a team member's objectives |
| `to-be-discussed` | Needs a team meeting to align before proceeding |
| `Discussion` | Open-ended discussion, not yet actionable work |
| `Documentation` | Documentation-only change |
| `curadoria` | Curation-related (collection/seed curation, not engineering) |
| `dependencies` | Auto-filed by Dependabot-style tooling for dependency-file updates |
| `enhancement` | Legacy/generic enhancement label, largely superseded by `Type-Enhancement` |

## Filing an issue

Pick the `Component-*` label(s) for every affected part of the system (an issue can span more than one, e.g. a Replay change that also touches ContinuousIntegration), plus one `Type-*` and one `Impact-*` label. Use `to-be-discussed` if the approach isn't settled yet, and `objectives` if the issue represents a standing goal rather than a one-off fix.
