db = new Mongo().getDB("ESD");

db.createCollection("CompanyProfile");
db.CompanyProfile.insert([
    {
        _id: 1,
        name: "資策會",
        updateTime: new Date("2021-04-11T00:00:00Z"),
        createTime: new Date("2021-04-11T00:00:00Z"),
    },
]);
db.createCollection("PolicyProfile");
db.PolicyProfile.insert([
    {
        _id: 1,
        name: "聚合調度",
        updateTime: new Date("2021-04-11T00:00:00Z"),
        createTime: new Date("2021-04-11T00:00:00Z"),
        l1: {mode: 1, dayType: 1},
        l2: {
            items: [
                [1, 1, 1, 1, 1],
                [1, 1, 1, 1, 1],
                [1, 1, 1, 1, 1],
                [1, 1, 1, 1, 1],
                [1, 1, 1, 1, 1],
            ],
        },
        l3: {timeslotType: 1, releaseType: 1},
    },
]);
db.createCollection("UserProfile");
db.UserProfile.insert([
    {
        _id: 1,
        name: "系統管理者",
        email: "admin@iii.org.tw",
        password: '$2a$10$lTRukYGHSeVFqZ5MPjmFL.AF/A3C0DDmGWEMN2Mb4k40RD5Rs16lq',
        companyId: DBRef("CompanyProfile", 1),
        srId: null,
        enableStatus: "enable",
        updateTime: new Date("2021-04-11T00:00:00Z"),
        createTime: new Date("2021-04-11T00:00:00Z"),
        roleIds: [1,],
        lastLoginTime: new Date("2021-04-11T00:00:00Z"),
        retry: 0,
    },
]);

db.createCollection("sequence");
db.sequence.insert([
    {_id: "CompanyProfile", seqId: 1},
    {_id: "UserProfile", seqId: 1},
    {_id: "PolicyProfile", seqId: 1},

    {_id: "ElectricPrice", seqId: 0},
    {_id: "FieldProfile", seqId: 0},
    {_id: "AutomaticFrequencyControlProfile", seqId: 0},
    {_id: "DemandResponseProfile", seqId: 0},
    {_id: "SpinReserveProfile", seqId: 0},
]);

db.createCollection("TOUS");
db.TOUS.insert([{
    "_id": 14,
    "type": "TPH3S",
    "Active_StartDate": ISODate("2013-10-01T00:00:00.000+08:00"),
    "Summer_Regular_Contarct": 223.6,
    "NonSummer_Regular_Contarct": 166.9,
    "Summer_HalfPeak_Contarct": 166.9,
    "NonSummer_HalfPeak_Contarct": 166.9,
    "Summer_SaturdayHalfPeak_Contarct": 44.7,
    "NonSummer_SaturdayHalfPeak_Contarct": 33.3,
    "Summer_OffPeak_Contarct": 44.7,
    "NonSummer_OffPeak_Contarct": 33.3,
    "Summer_NormalDay_Peak": 4.98,
    "Summer_NormalDay_HalfPeak": 3.37,
    "NonSummer_NormalDay_HalfPeak": 3.29,
    "Summer_NormalDay_OffPeak": 1.84,
    "NonSummer_NormalDay_OffPeak": 1.77,
    "Summer_Saturday_HalfPeak": 2.44,
    "NonSummer_Saturday_HalfPeak": 2.36,
    "Summer_Saturday_OffPeak": 1.84,
    "NonSummer_Saturday_OffPeak": 1.77,
    "Summer_OffPeakDay_OffPeak": 1.84,
    "NonSummer_OffPeakDay_OffPeak": 1.77,
    "PF_Adj": 0.027
},
    {
        "_id": 13,
        "type": "TPH3S",
        "Active_StartDate": ISODate("2015-04-01T00:00:00.000+08:00"),
        "Summer_Regular_Contarct": 223.6,
        "NonSummer_Regular_Contarct": 166.9,
        "Summer_HalfPeak_Contarct": 166.9,
        "NonSummer_HalfPeak_Contarct": 166.9,
        "Summer_SaturdayHalfPeak_Contarct": 44.7,
        "NonSummer_SaturdayHalfPeak_Contarct": 33.3,
        "Summer_OffPeak_Contarct": 44.7,
        "NonSummer_OffPeak_Contarct": 33.3,
        "Summer_NormalDay_Peak": 4.76,
        "Summer_NormalDay_HalfPeak": 3.15,
        "NonSummer_NormalDay_HalfPeak": 3.08,
        "Summer_NormalDay_OffPeak": 1.59,
        "NonSummer_NormalDay_OffPeak": 1.53,
        "Summer_Saturday_HalfPeak": 2.21,
        "NonSummer_Saturday_HalfPeak": 2.13,
        "Summer_Saturday_OffPeak": 1.59,
        "NonSummer_Saturday_OffPeak": 1.53,
        "Summer_OffPeakDay_OffPeak": 1.59,
        "NonSummer_OffPeakDay_OffPeak": 1.53,
        "PF_Adj": 0.027
    },
    {
        "_id": 12,
        "type": "TPH3S",
        "Active_StartDate": ISODate("2015-10-01T00:00:00.000+08:00"),
        "Summer_Regular_Contarct": 223.6,
        "NonSummer_Regular_Contarct": 166.9,
        "Summer_HalfPeak_Contarct": 166.9,
        "NonSummer_HalfPeak_Contarct": 166.9,
        "Summer_SaturdayHalfPeak_Contarct": 44.7,
        "NonSummer_SaturdayHalfPeak_Contarct": 33.3,
        "Summer_OffPeak_Contarct": 44.7,
        "NonSummer_OffPeak_Contarct": 33.3,
        "Summer_NormalDay_Peak": 4.73,
        "Summer_NormalDay_HalfPeak": 3.11,
        "NonSummer_NormalDay_HalfPeak": 3.03,
        "Summer_NormalDay_OffPeak": 1.5,
        "NonSummer_NormalDay_OffPeak": 1.44,
        "Summer_Saturday_HalfPeak": 2.14,
        "NonSummer_Saturday_HalfPeak": 2.06,
        "Summer_Saturday_OffPeak": 1.5,
        "NonSummer_Saturday_OffPeak": 1.44,
        "Summer_OffPeakDay_OffPeak": 1.5,
        "NonSummer_OffPeakDay_OffPeak": 1.44,
        "PF_Adj": 0.027
    },
    {
        "_id": 11,
        "type": "TPH3S",
        "Active_StartDate": ISODate("2016-04-01T00:00:00.000+08:00"),
        "Summer_Regular_Contarct": 223.6,
        "NonSummer_Regular_Contarct": 166.9,
        "Summer_HalfPeak_Contarct": 166.9,
        "NonSummer_HalfPeak_Contarct": 166.9,
        "Summer_SaturdayHalfPeak_Contarct": 44.7,
        "NonSummer_SaturdayHalfPeak_Contarct": 33.3,
        "Summer_OffPeak_Contarct": 44.7,
        "NonSummer_OffPeak_Contarct": 33.3,
        "Summer_NormalDay_Peak": 4.41,
        "Summer_NormalDay_HalfPeak": 2.76,
        "NonSummer_NormalDay_HalfPeak": 2.69,
        "Summer_NormalDay_OffPeak": 1.26,
        "NonSummer_NormalDay_OffPeak": 1.21,
        "Summer_Saturday_HalfPeak": 1.78,
        "NonSummer_Saturday_HalfPeak": 1.71,
        "Summer_Saturday_OffPeak": 1.26,
        "NonSummer_Saturday_OffPeak": 1.21,
        "Summer_OffPeakDay_OffPeak": 1.26,
        "NonSummer_OffPeakDay_OffPeak": 1.21,
        "PF_Adj": 0.015
    },
    {
        "_id": 10,
        "type": "TPH3S",
        "Active_StartDate": ISODate("2018-04-01T00:00:00.000+08:00"),
        "Summer_Regular_Contarct": 223.6,
        "NonSummer_Regular_Contarct": 166.9,
        "Summer_HalfPeak_Contarct": 166.9,
        "NonSummer_HalfPeak_Contarct": 166.9,
        "Summer_SaturdayHalfPeak_Contarct": 44.7,
        "NonSummer_SaturdayHalfPeak_Contarct": 33.3,
        "Summer_OffPeak_Contarct": 44.7,
        "NonSummer_OffPeak_Contarct": 33.3,
        "Summer_NormalDay_Peak": 4.67,
        "Summer_NormalDay_HalfPeak": 2.9,
        "NonSummer_NormalDay_HalfPeak": 2.82,
        "Summer_NormalDay_OffPeak": 1.32,
        "NonSummer_NormalDay_OffPeak": 1.26,
        "Summer_Saturday_HalfPeak": 1.78,
        "NonSummer_Saturday_HalfPeak": 1.71,
        "Summer_Saturday_OffPeak": 1.32,
        "NonSummer_Saturday_OffPeak": 1.26,
        "Summer_OffPeakDay_OffPeak": 1.32,
        "NonSummer_OffPeakDay_OffPeak": 1.26,
        "PF_Adj": 0.015
    },
    {
        "_id": 1,
        "type": "TPMRL2S",
        "Active_StartDate": ISODate("2018-01-01T00:00:00.000+08:00"),
        "basic": 75,
        "overcost": 0.96,
        "summer": {
            "n_t_peak": 4.44,
            "n_o_peak": 1.8,
            "h_peak": 1.8
        },
        "nonsummer": {
            "n_t_peak": 4.23,
            "n_o_peak": 1.73,
            "h_peak": 1.73
        }
    }]);
