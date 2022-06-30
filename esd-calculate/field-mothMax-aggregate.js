db.getCollection("ElectricData").aggregate(
    // Pipeline
    [
        // Stage 1
        {
            $match: {
                "fieldId": {"$ref": "FieldProfile", "$id": 999}, "dataType": "T3"
            }
        },

        // Stage 2
        {
            $project: {
                "time": "$time",
                "m2kW": "$m2kW",
                "m1kW": "$m1kW",
                "m0kW": "$m0kW",
                "m3kW": "$m3kW",
                "M0_2KW": {$subtract: ["$m0kW", "$m2kW"]}
            }
        },

        // Stage 3
        {
            $group: {
                "_id": {"fieldId": "$fieldId", "Year": {"$year": "$time"}, "Month": {"$month": "$time"}},
                "MAX_M0KW": {"$max": "$m0kW"},
                "MAX_M0_2KW": {"$max": "$M0_2KW"},
                "MAX_M1KW": {"$max": "$m1kW"},
                "M3KW": {"$sum": "$m3kW"},
                "m2kW": {"$sum": "$m2kW"}
            }
        },

        // Stage 4
        {
            $sort: {
                "_id.Year": 1,
                "_id.Month": 1
            }
        },

    ]

    // Created with Studio 3T, the IDE for MongoDB - https://studio3t.com/

);
