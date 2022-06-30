db.getCollection("KwEstimation").aggregate(

	// Pipeline
	[
		// Stage 1
		{
			$match: {
			"fieldId":NumberLong(999)
			}
		},

		// Stage 2
		{
			$group: {
				"_id":{"fieldId":"$fieldId","category":"$category","group":"$group"},
				"count":{"$sum":1}
			}
		},

		// Stage 3
		{
			$sort: {
			    "_id.category":1,
			"_id.group":1
			
			}
		},

	]

	// Created with Studio 3T, the IDE for MongoDB - https://studio3t.com/

);
