{
    "id": "3f9b42ee-da0e-41a0-9dcd-5c59096cee98",
    "version": 265,
    "projectId": "e74c1b9d-16ff-4a29-8c4c-e3147877de1b",
    "teamId": "f6c0b8cb-de69-495c-980b-2b0e0a6ca288",
    "title": "new title htduyj",
    "displayId": 0,
    "cycleNumber": 0,
    "feedback": "DISABLED",
    "flowId": "string",
    "type": "QA",
    "state": "PAUSED",
    "activeStatus": "ACTIVE",
    "assignmentLeaseExpiry": 60,
    "judgmentsPerRow": 13,
    "rowsPerPage": 1,
    "displayContributorName": true,
    "judgmentModifiable": true,
    "sendBackOp": "SEND_BACK_TO_POOL",
    "copiedFrom": null,
    "maxJudgmentPerContributorEnabled": true,
    "maxJudgmentPerContributor": 0,
    "maxSkippablePromptNum": 0,
    "unitSegmentType": "UNIT_ONLY",
    "internalSecret": "V1TeRYacyzXeb0hzQzMLzM6iQGBm5dgkMYg7wpF1PJyCfi",
    "jobCml": {
        "id": "067c0bf2-d148-488a-9743-646bf7fb8151",
        "version": 2,
        "teamId": "f6c0b8cb-de69-495c-980b-2b0e0a6ca288",
        "jobId": "3f9b42ee-da0e-41a0-9dcd-5c59096cee98",
        "instructions": "<h1>Overview</h1>\n<hr/>\n\n<h1>Steps</h1>\n<hr/>\n\n<h1>Rules &amp; Tips</h1>\n<hr/>\n\n<h1>Examples</h1>\n<hr/>\n\n",
        "cml": "<div class=\"html-element-wrapper\">Show data to contributors here</div><cml:radios label=\"Ask question here:\" validates=\"required\" gold=\"true\"><cml:radio label=\"First option\" value=\"first_option\" /><cml:radio label=\"Second option\" value=\"second_option\" /></cml:radios>",
        "js": "",
        "css": "",
        "validators": [
            "required"
        ],
        "isValid": null,
        "tags": [
            "radios",
            "radio"
        ],
        "editorType": "GRAPHICAL_EDITOR"
    },
    "jobFilter": {
        "id": "c431145c-b1b7-415c-9612-a7c133622702",
        "version": 1,
        "teamId": "f6c0b8cb-de69-495c-980b-2b0e0a6ca288",
        "projectId": "e74c1b9d-16ff-4a29-8c4c-e3147877de1b",
        "recursive": false,
        "origin": "project_data_source",
        "appliedJobId": "3f9b42ee-da0e-41a0-9dcd-5c59096cee98",
        "sampleRate": 0,
        "fixedUnitNum": 0,
        "schedulerDelay": 0,
        "schedulerUnit": "HOURS",
        "filterCriteria": "{}",
        "filterNum": 0,
        "segmental": false,
        "carryJudgment": true,
        "overwriteJudgment": true,
        "segmentSampleRate": 100,
        "minimumSampleUnit": 0
    },
    "jobCrowd": {
        "id": "4e38d34a-8adb-4750-b3c2-59a857901d1a",
        "version": 0,
        "projectId": "e74c1b9d-16ff-4a29-8c4c-e3147877de1b",
        "jobId": "3f9b42ee-da0e-41a0-9dcd-5c59096cee98",
        "teamId": "f6c0b8cb-de69-495c-980b-2b0e0a6ca288",
        "crowdType": [
            "INTERNAL"
        ],
        "crowdSubType": null,
        "channels": [],
        "customChannels": [],
        "level": 0,
        "languages": [],
        "countryType": "all",
        "countries": [],
        "containExplicit": false,
        "curatedCrowd": false
    },
    "testQuestionSettings": null,
    "operateHistoryId": "400681a4-d27e-485d-9491-ae9fadec9cd8",
    "payRateType": "",
    "invoiceStatisticsType": "UNIT_COUNT",
    "createdAt": "2025-06-30T04:36:51.122Z",
    "allowSelfQa": true,
    "allowAbandonUnits": true,
    "maxNumberOfAllowedAbandonment": 0,
    "sendAbandonedUnitsBackToJob": true,
    "instruction": "Updated instructions",
    "validationRuleId": null,
    "enableBlueTooth": true,
    "launchPlatform": [],
    "latestAppVersion": true,
    "reusablePrompt": true,
    "reusablePin": true,
    "totalSessionNumber": 0,
    "firstLaunchTime": null,
    "promptsEnabling": null,
    "reviewReasons": "",
    "reviewReasonOption": "SINGLE",
    "parentJobId": null,
    "unitGroupOption": "RETAIN",
    "appendable": false,
    "enableShareLink": true,
    "timeLimitThresholdSec": 0,
    "enableExternalAutoAssignJob": true,
    "dynamicJudgements": {
        "enableDynamicJudgements": false,
        "maxDynamicJudgements": 0,
        "mode": "MINIMUM_CONFIDENCE",
        "minimumConfidence": 0.0,
        "enabledQuestions": "{}",
        "id": "",
        "version": 0
    },
    "enableExpirationTime": 0,
    "expirationDurationSet": 0,
    "expirationDurationUpdate": 0,
    "threshold": 0.0,
    "qaAdvancedOptions": null,
    "active": true,
    "running": false,
    "alias": "Q0",
    "appenConnectCrowd": false,
    "draft": false,
    "multipleJudgment": true,
    "segmented": false
}



jobCml -> Validations are set
jobFilter
jobCrowd
testQuestionSettings -> Validations are set
dynamicJudgements ->  validations

Mande required:
    id, projectId, teamId, title, type
    Title must be unique (60080 if duplicate); body teamId must match query teamId

Configuration:
    assignmentLeaseExpiry: integer, minimum 60
    judgmentsPerRow: integer ≥ 1
    rowsPerPage: integer ≥ 1
    timeLimitThresholdSec: percentage 0–100 (0 = disabled)

Expiration:
    enableExpirationTime: 0 = disabled, >0 = enabled
    When enabled: expirationDurationSet > 0, expirationDurationUpdate > 0
    All expiration fields accept 0 = disabled

jobCml -> Validations are set:
    Must contain at least one cml:* element
    Payload < 2MB; no <script> or javascript:
    Attribute values must be quoted
    If dynamicJudgements.enableDynamicJudgements = true: require at least one labeled CML question (label attr)
    JSON-escape quotes/backslashes when embedding in JSON

dynamicJudgements -> Validations are set:
    enableDynamicJudgements: required (boolean)
    maxDynamicJudgements: > 0
    mode: MINIMUM_CONFIDENCE or MATCHING_JUDGEMENTS
    enabledQuestions: non-empty, valid JSON
    minimumConfidence: 0.0–1.0 (0 allowed)
    matchingJudgements: > 0
    Business rules when enabled:
        Provide either minimumConfidence or matchingJudgements, not both
        MINIMUM_CONFIDENCE: minimumConfidence required; matchingJudgements must be null
        MATCHING_JUDGEMENTS: matchingJudgements required; minimumConfidence must be null

testQuestionSettings -> Validations are set:
    If provided: testQuestionSettings.jobId and testQuestionSettings.teamId must match main job
    Mode must be valid (QUIZ_WORK/WORK_ONLY/QUIZ_ONLY)

jobFilter:
    Documented as part of response structure (ids, flags, scheduler fields); no extra numeric constraints beyond types

jobCrowd:
Documented structure (crowdType, channels, country filters, flags); no extra numeric constraints beyond types

Enums and other fields:
sendBackOp: SEND_BACK_TO_SAME_CONTRIBUTOR, SEND_BACK_TO_ANY_CONTRIBUTOR, SEND_BACK_TO_POOL, IGNORE
unitSegmentType: UNIT_ONLY, UNIT_SEGMENT, UNIT_WITH_PREVIEW
invoiceStatisticsType: UNIT_COUNT (as per example)
Common flags and metadata (displayContributorName, judgmentModifiable, allowSelfQa, etc.) documented

Common errors:
400: missing/invalid fields, teamId mismatch
60080: job title already exists
60000: not found/access denied
500: internal error (ensure required fields valid)

200 response example:
Now includes the full object you provided (all sections: jobCml, jobFilter, jobCrowd, testQuestionSettings, dynamicJudgements, and all scalar fields).
