
/**
 * Client
**/

import * as runtime from './runtime/library.js';
import $Types = runtime.Types // general types
import $Public = runtime.Types.Public
import $Utils = runtime.Types.Utils
import $Extensions = runtime.Types.Extensions
import $Result = runtime.Types.Result

export type PrismaPromise<T> = $Public.PrismaPromise<T>


/**
 * Model User
 * 
 */
export type User = $Result.DefaultSelection<Prisma.$UserPayload>
/**
 * Model Client
 * 
 */
export type Client = $Result.DefaultSelection<Prisma.$ClientPayload>
/**
 * Model Detalii
 * 
 */
export type Detalii = $Result.DefaultSelection<Prisma.$DetaliiPayload>
/**
 * Model PunctDeLucru
 * 
 */
export type PunctDeLucru = $Result.DefaultSelection<Prisma.$PunctDeLucruPayload>
/**
 * Model Istoric
 * 
 */
export type Istoric = $Result.DefaultSelection<Prisma.$IstoricPayload>
/**
 * Model Task
 * 
 */
export type Task = $Result.DefaultSelection<Prisma.$TaskPayload>
/**
 * Model Rule
 * 
 */
export type Rule = $Result.DefaultSelection<Prisma.$RulePayload>
/**
 * Model RuleCondition
 * 
 */
export type RuleCondition = $Result.DefaultSelection<Prisma.$RuleConditionPayload>
/**
 * Model UserClient
 * 
 */
export type UserClient = $Result.DefaultSelection<Prisma.$UserClientPayload>

/**
 * Enums
 */
export namespace $Enums {
  export const Role: {
  ADMIN: 'ADMIN',
  USER: 'USER',
  MANAGER: 'MANAGER'
};

export type Role = (typeof Role)[keyof typeof Role]


export const Tip: {
  SRL: 'SRL',
  PFA: 'PFA',
  II: 'II',
  ASOC: 'ASOC',
  SA: 'SA',
  SPARL: 'SPARL'
};

export type Tip = (typeof Tip)[keyof typeof Tip]


export const DaNuNuECazul: {
  DA: 'DA',
  NU: 'NU',
  NU_E_CAZUL: 'NU_E_CAZUL'
};

export type DaNuNuECazul = (typeof DaNuNuECazul)[keyof typeof DaNuNuECazul]


export const Impozit: {
  MICRO_1: 'MICRO_1',
  MICRO_3: 'MICRO_3',
  PROFIT: 'PROFIT'
};

export type Impozit = (typeof Impozit)[keyof typeof Impozit]


export const DaLunarTrim: {
  DA_LUNAR: 'DA_LUNAR',
  DA_TRIM: 'DA_TRIM',
  NU: 'NU'
};

export type DaLunarTrim = (typeof DaLunarTrim)[keyof typeof DaLunarTrim]


export const Administratie: {
  DGRF_BUCURESTI: 'DGRF_BUCURESTI',
  AFCM_DGRF_BUCURESTI: 'AFCM_DGRF_BUCURESTI',
  AFCN: 'AFCN',
  SECTOR_1: 'SECTOR_1',
  SECTOR_2: 'SECTOR_2',
  SECTOR_3: 'SECTOR_3',
  SECTOR_4: 'SECTOR_4',
  SECTOR_5: 'SECTOR_5',
  SECTOR_6: 'SECTOR_6',
  AJFP_ILFOV: 'AJFP_ILFOV',
  UFO_BUFTEA: 'UFO_BUFTEA',
  UFO_BRAGADIRU: 'UFO_BRAGADIRU',
  DGRF_BRASOV: 'DGRF_BRASOV',
  AJFP_ALBA: 'AJFP_ALBA',
  UFM_AIUD: 'UFM_AIUD',
  UFM_BLAJ: 'UFM_BLAJ',
  UFM_SEBES: 'UFM_SEBES',
  UFO_CAMPENI: 'UFO_CAMPENI',
  UFO_CUGIR: 'UFO_CUGIR',
  UFO_ZLATNA: 'UFO_ZLATNA',
  AJFP_BRASOV: 'AJFP_BRASOV',
  UFM_FAGARAS: 'UFM_FAGARAS',
  UFM_SACELE: 'UFM_SACELE',
  UFM_CODLEA: 'UFM_CODLEA',
  UFO_RUPEA: 'UFO_RUPEA',
  UFO_VICTORIA: 'UFO_VICTORIA',
  UFO_ZARNESTI: 'UFO_ZARNESTI',
  UFO_RASNOV: 'UFO_RASNOV',
  AJFP_COVASNA: 'AJFP_COVASNA',
  UFM_TARGU_SECUIESC: 'UFM_TARGU_SECUIESC',
  UFO_BARAOLT: 'UFO_BARAOLT',
  AJFP_HARGHITA: 'AJFP_HARGHITA',
  UFM_ODORHEIU_SECUIESC: 'UFM_ODORHEIU_SECUIESC',
  UFM_TOPLITA: 'UFM_TOPLITA',
  UFM_GHEORGHENI: 'UFM_GHEORGHENI',
  AJFP_MURES: 'AJFP_MURES',
  UFM_SIGHISOARA: 'UFM_SIGHISOARA',
  UFM_REGHIN: 'UFM_REGHIN',
  UFM_TARNAVENI: 'UFM_TARNAVENI',
  UFO_LUDUS: 'UFO_LUDUS',
  UFO_SOVATA: 'UFO_SOVATA',
  AJFP_SIBIU: 'AJFP_SIBIU',
  UFM_MEDIAS: 'UFM_MEDIAS',
  UFO_AGNITA: 'UFO_AGNITA',
  UFO_SALISTE: 'UFO_SALISTE',
  UFO_AVRIG: 'UFO_AVRIG',
  DGRF_CLUJ_NAPOCA: 'DGRF_CLUJ_NAPOCA',
  AJFP_BIHOR: 'AJFP_BIHOR',
  UFO_ALESD: 'UFO_ALESD',
  UFO_BEIUS: 'UFO_BEIUS',
  UFO_MARGHITA: 'UFO_MARGHITA',
  UFO_SALONTA: 'UFO_SALONTA',
  AJFP_BISTRITA_NASAUD: 'AJFP_BISTRITA_NASAUD',
  UFO_BECLEAN: 'UFO_BECLEAN',
  UFO_NASAUD: 'UFO_NASAUD',
  UFO_SANGEORZ_BAI: 'UFO_SANGEORZ_BAI',
  AJFP_CLUJ: 'AJFP_CLUJ',
  UFM_TURDA: 'UFM_TURDA',
  UFM_DEJ: 'UFM_DEJ',
  UFM_GHERLA: 'UFM_GHERLA',
  UFO_HUEDIN: 'UFO_HUEDIN',
  AJFP_MARAMURES: 'AJFP_MARAMURES',
  UFM_SIGHETU_MARMATIEI: 'UFM_SIGHETU_MARMATIEI',
  UFO_TARGU_LAPUS: 'UFO_TARGU_LAPUS',
  UFO_VISEU_DE_SUS: 'UFO_VISEU_DE_SUS',
  AJFP_SATU_MARE: 'AJFP_SATU_MARE',
  UFM_CAREI: 'UFM_CAREI',
  UFO_NEGRESTI_OAS: 'UFO_NEGRESTI_OAS',
  UFO_TASNAD: 'UFO_TASNAD',
  AJFP_SALAJ: 'AJFP_SALAJ',
  UFO_SIMLEU_SILVANIEI: 'UFO_SIMLEU_SILVANIEI',
  UFO_JIBOU: 'UFO_JIBOU',
  UFO_CEHU_SILVANIEI: 'UFO_CEHU_SILVANIEI',
  DGRF_CRAIOVA: 'DGRF_CRAIOVA',
  AJFP_DOLJ: 'AJFP_DOLJ',
  UFO_CALAFAT: 'UFO_CALAFAT',
  UFO_FILIASI: 'UFO_FILIASI',
  UFO_SEGARCEA: 'UFO_SEGARCEA',
  UFO_BECHET: 'UFO_BECHET',
  UFO_BAILESTI: 'UFO_BAILESTI',
  AJFP_GORJ: 'AJFP_GORJ',
  UFO_MOTRU: 'UFO_MOTRU',
  UFO_NOVACI: 'UFO_NOVACI',
  UFO_TARGU_CARBUNESTI: 'UFO_TARGU_CARBUNESTI',
  UFO_ROVINARI: 'UFO_ROVINARI',
  AJFP_MEHEDINTI: 'AJFP_MEHEDINTI',
  UFO_STREHAIA: 'UFO_STREHAIA',
  UFO_ORSOVA: 'UFO_ORSOVA',
  UFO_VANJU_MARE: 'UFO_VANJU_MARE',
  UFO_BAIA_DE_ARAMA: 'UFO_BAIA_DE_ARAMA',
  AJFP_OLT: 'AJFP_OLT',
  UFM_CARACAL: 'UFM_CARACAL',
  UFO_BALS: 'UFO_BALS',
  UFO_CORABIA: 'UFO_CORABIA',
  AJFP_VALCEA: 'AJFP_VALCEA',
  UFM_DRAGASANI: 'UFM_DRAGASANI',
  UFO_BALCESTI: 'UFO_BALCESTI',
  UFO_GURA_LOTRULUI: 'UFO_GURA_LOTRULUI',
  UFO_HOREZU: 'UFO_HOREZU',
  UFO_BABENI: 'UFO_BABENI',
  DGRF_GALATI: 'DGRF_GALATI',
  AJFP_BRAILA: 'AJFP_BRAILA',
  UFO_INSURATEI: 'UFO_INSURATEI',
  UFO_FAUREI: 'UFO_FAUREI',
  UFO_IANCA: 'UFO_IANCA',
  AJFP_BUZAU: 'AJFP_BUZAU',
  UFM_RAMNICU_SARAT: 'UFM_RAMNICU_SARAT',
  UFO_POGOANELE: 'UFO_POGOANELE',
  UFO_PATARLAGELE: 'UFO_PATARLAGELE',
  AJFP_CONSTANTA: 'AJFP_CONSTANTA',
  UFM_MANGALIA: 'UFM_MANGALIA',
  UFM_MEDGIDIA: 'UFM_MEDGIDIA',
  UFO_EFORIE: 'UFO_EFORIE',
  UFO_HARSOVA: 'UFO_HARSOVA',
  UFO_BANEASA: 'UFO_BANEASA',
  AJFP_GALATI: 'AJFP_GALATI',
  UFM_TECUCI: 'UFM_TECUCI',
  UFO_TARGU_BUJOR: 'UFO_TARGU_BUJOR',
  AJFP_TULCEA: 'AJFP_TULCEA',
  UFO_SULINA: 'UFO_SULINA',
  UFO_BABADAG: 'UFO_BABADAG',
  UFO_MACIN: 'UFO_MACIN',
  UFC_BAIA: 'UFC_BAIA',
  AJFP_VRANCEA: 'AJFP_VRANCEA',
  UFM_ADJUD: 'UFM_ADJUD',
  UFO_PANCIU: 'UFO_PANCIU',
  DGRF_IASI: 'DGRF_IASI',
  AJFP_BACAU: 'AJFP_BACAU',
  UFM_ONESTI: 'UFM_ONESTI',
  UFO_BUHUSI: 'UFO_BUHUSI',
  UFO_MOINESTI: 'UFO_MOINESTI',
  UFC_PODU_TURCULUI: 'UFC_PODU_TURCULUI',
  AJFP_BOTOSANI: 'AJFP_BOTOSANI',
  UFM_DOROHOI: 'UFM_DOROHOI',
  UFO_DARABANI: 'UFO_DARABANI',
  UFO_SAVENI: 'UFO_SAVENI',
  AJFP_IASI: 'AJFP_IASI',
  UFM_PASCANI: 'UFM_PASCANI',
  UFO_HARLAU: 'UFO_HARLAU',
  UFO_TARGU_FRUMOS: 'UFO_TARGU_FRUMOS',
  UFC_RADUCANENI: 'UFC_RADUCANENI',
  AJFP_NEAMT: 'AJFP_NEAMT',
  UFM_ROMAN: 'UFM_ROMAN',
  UFO_TARGU_NEAMT: 'UFO_TARGU_NEAMT',
  UFO_BICAZ: 'UFO_BICAZ',
  AJFP_SUCEAVA: 'AJFP_SUCEAVA',
  UFM_CAMPULUNG_MOLDOVENESC: 'UFM_CAMPULUNG_MOLDOVENESC',
  UFM_FALTICENI: 'UFM_FALTICENI',
  UFM_RADAUTI: 'UFM_RADAUTI',
  UFM_VATRA_DORNEI: 'UFM_VATRA_DORNEI',
  UFO_GURA_HUMORULUI: 'UFO_GURA_HUMORULUI',
  UFO_SIRET: 'UFO_SIRET',
  AJFP_VASLUI: 'AJFP_VASLUI',
  UFM_BARLAD: 'UFM_BARLAD',
  UFM_HUSI: 'UFM_HUSI',
  UFO_NEGRESTI: 'UFO_NEGRESTI',
  DGRF_PLOIESTI: 'DGRF_PLOIESTI',
  AJFP_ARGES: 'AJFP_ARGES',
  UFM_CAMPULUNG: 'UFM_CAMPULUNG',
  UFM_CURTEA_DE_ARGES: 'UFM_CURTEA_DE_ARGES',
  UFO_COSTESTI: 'UFO_COSTESTI',
  UFO_TOPOLOVENI: 'UFO_TOPOLOVENI',
  UFO_MIOVENI: 'UFO_MIOVENI',
  AJFP_CALARASI: 'AJFP_CALARASI',
  UFM_OLTENITA: 'UFM_OLTENITA',
  UFO_LEHLIU: 'UFO_LEHLIU',
  UFO_BUDESTI: 'UFO_BUDESTI',
  AJFP_DAMBOVITA: 'AJFP_DAMBOVITA',
  UFM_MORENI: 'UFM_MORENI',
  UFO_PUCIOASA: 'UFO_PUCIOASA',
  UFO_GAESTI: 'UFO_GAESTI',
  UFO_TITU: 'UFO_TITU',
  AJFP_GIURGIU: 'AJFP_GIURGIU',
  UFO_BOLINTIN_VALE: 'UFO_BOLINTIN_VALE',
  UFO_MIHAILESTI: 'UFO_MIHAILESTI',
  AJFP_IALOMITA: 'AJFP_IALOMITA',
  UFM_FETESTI: 'UFM_FETESTI',
  UFM_URZICENI: 'UFM_URZICENI',
  AJFP_PRAHOVA: 'AJFP_PRAHOVA',
  UFM_CAMPINA: 'UFM_CAMPINA',
  UFO_BAICOI: 'UFO_BAICOI',
  UFO_BUSTENI: 'UFO_BUSTENI',
  UFO_BOLDESTI_SCAENI: 'UFO_BOLDESTI_SCAENI',
  UFO_MIZIL: 'UFO_MIZIL',
  UFO_SLANIC: 'UFO_SLANIC',
  UFO_VALENII_DE_MUNTE: 'UFO_VALENII_DE_MUNTE',
  AJFP_TELEORMAN: 'AJFP_TELEORMAN',
  UFM_TURNU_MAGURELE: 'UFM_TURNU_MAGURELE',
  UFM_ROSIORII_DE_VEDE: 'UFM_ROSIORII_DE_VEDE',
  UFO_VIDELE: 'UFO_VIDELE',
  UFO_ZIMNICEA: 'UFO_ZIMNICEA',
  DGRF_TIMISOARA: 'DGRF_TIMISOARA',
  AJFP_ARAD: 'AJFP_ARAD',
  UFO_CHISINEU_CRIS: 'UFO_CHISINEU_CRIS',
  UFO_LIPOVA: 'UFO_LIPOVA',
  UFO_INEU: 'UFO_INEU',
  UFO_SEBIS: 'UFO_SEBIS',
  SFC_SAVARSIN: 'SFC_SAVARSIN',
  AJFP_CARAS_SEVERIN: 'AJFP_CARAS_SEVERIN',
  UFM_CARANSEBES: 'UFM_CARANSEBES',
  UFO_BAILE_HERCULANE: 'UFO_BAILE_HERCULANE',
  UFO_MOLDOVA_NOUA: 'UFO_MOLDOVA_NOUA',
  UFO_ORAVITA: 'UFO_ORAVITA',
  UFO_OTELU_ROSU: 'UFO_OTELU_ROSU',
  SFC_BOZOVICI: 'SFC_BOZOVICI',
  AJFP_HUNEDOARA: 'AJFP_HUNEDOARA',
  UFM_BRAD: 'UFM_BRAD',
  UFM_HUNEDOARA: 'UFM_HUNEDOARA',
  UFM_ORASTIE: 'UFM_ORASTIE',
  UFM_PETROSANI: 'UFM_PETROSANI',
  UFO_HATEG: 'UFO_HATEG',
  AJFP_TIMIS: 'AJFP_TIMIS',
  UFM_LUGOJ: 'UFM_LUGOJ',
  UFO_BUZIAS: 'UFO_BUZIAS',
  UFO_DETA: 'UFO_DETA',
  UFO_FAGET: 'UFO_FAGET',
  UFO_JIMBOLIA: 'UFO_JIMBOLIA',
  UFO_SANNICOLAU_MARE: 'UFO_SANNICOLAU_MARE'
};

export type Administratie = (typeof Administratie)[keyof typeof Administratie]


export const ConditionOperator: {
  EQUALS: 'EQUALS',
  NOT_EQUALS: 'NOT_EQUALS',
  IS_TRUE: 'IS_TRUE',
  IS_FALSE: 'IS_FALSE',
  IN: 'IN'
};

export type ConditionOperator = (typeof ConditionOperator)[keyof typeof ConditionOperator]


export const Frequency: {
  MONTHLY: 'MONTHLY',
  QUARTERLY: 'QUARTERLY',
  YEARLY: 'YEARLY',
  CONDITIONAL: 'CONDITIONAL'
};

export type Frequency = (typeof Frequency)[keyof typeof Frequency]

}

export type Role = $Enums.Role

export const Role: typeof $Enums.Role

export type Tip = $Enums.Tip

export const Tip: typeof $Enums.Tip

export type DaNuNuECazul = $Enums.DaNuNuECazul

export const DaNuNuECazul: typeof $Enums.DaNuNuECazul

export type Impozit = $Enums.Impozit

export const Impozit: typeof $Enums.Impozit

export type DaLunarTrim = $Enums.DaLunarTrim

export const DaLunarTrim: typeof $Enums.DaLunarTrim

export type Administratie = $Enums.Administratie

export const Administratie: typeof $Enums.Administratie

export type ConditionOperator = $Enums.ConditionOperator

export const ConditionOperator: typeof $Enums.ConditionOperator

export type Frequency = $Enums.Frequency

export const Frequency: typeof $Enums.Frequency

/**
 * ##  Prisma Client ʲˢ
 *
 * Type-safe database client for TypeScript & Node.js
 * @example
 * ```
 * const prisma = new PrismaClient()
 * // Fetch zero or more Users
 * const users = await prisma.user.findMany()
 * ```
 *
 *
 * Read more in our [docs](https://www.prisma.io/docs/reference/tools-and-interfaces/prisma-client).
 */
export class PrismaClient<
  ClientOptions extends Prisma.PrismaClientOptions = Prisma.PrismaClientOptions,
  const U = 'log' extends keyof ClientOptions ? ClientOptions['log'] extends Array<Prisma.LogLevel | Prisma.LogDefinition> ? Prisma.GetEvents<ClientOptions['log']> : never : never,
  ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs
> {
  [K: symbol]: { types: Prisma.TypeMap<ExtArgs>['other'] }

    /**
   * ##  Prisma Client ʲˢ
   *
   * Type-safe database client for TypeScript & Node.js
   * @example
   * ```
   * const prisma = new PrismaClient()
   * // Fetch zero or more Users
   * const users = await prisma.user.findMany()
   * ```
   *
   *
   * Read more in our [docs](https://www.prisma.io/docs/reference/tools-and-interfaces/prisma-client).
   */

  constructor(optionsArg ?: Prisma.Subset<ClientOptions, Prisma.PrismaClientOptions>);
  $on<V extends U>(eventType: V, callback: (event: V extends 'query' ? Prisma.QueryEvent : Prisma.LogEvent) => void): PrismaClient;

  /**
   * Connect with the database
   */
  $connect(): $Utils.JsPromise<void>;

  /**
   * Disconnect from the database
   */
  $disconnect(): $Utils.JsPromise<void>;

/**
   * Executes a prepared raw query and returns the number of affected rows.
   * @example
   * ```
   * const result = await prisma.$executeRaw`UPDATE User SET cool = ${true} WHERE email = ${'user@email.com'};`
   * ```
   *
   * Read more in our [docs](https://www.prisma.io/docs/reference/tools-and-interfaces/prisma-client/raw-database-access).
   */
  $executeRaw<T = unknown>(query: TemplateStringsArray | Prisma.Sql, ...values: any[]): Prisma.PrismaPromise<number>;

  /**
   * Executes a raw query and returns the number of affected rows.
   * Susceptible to SQL injections, see documentation.
   * @example
   * ```
   * const result = await prisma.$executeRawUnsafe('UPDATE User SET cool = $1 WHERE email = $2 ;', true, 'user@email.com')
   * ```
   *
   * Read more in our [docs](https://www.prisma.io/docs/reference/tools-and-interfaces/prisma-client/raw-database-access).
   */
  $executeRawUnsafe<T = unknown>(query: string, ...values: any[]): Prisma.PrismaPromise<number>;

  /**
   * Performs a prepared raw query and returns the `SELECT` data.
   * @example
   * ```
   * const result = await prisma.$queryRaw`SELECT * FROM User WHERE id = ${1} OR email = ${'user@email.com'};`
   * ```
   *
   * Read more in our [docs](https://www.prisma.io/docs/reference/tools-and-interfaces/prisma-client/raw-database-access).
   */
  $queryRaw<T = unknown>(query: TemplateStringsArray | Prisma.Sql, ...values: any[]): Prisma.PrismaPromise<T>;

  /**
   * Performs a raw query and returns the `SELECT` data.
   * Susceptible to SQL injections, see documentation.
   * @example
   * ```
   * const result = await prisma.$queryRawUnsafe('SELECT * FROM User WHERE id = $1 OR email = $2;', 1, 'user@email.com')
   * ```
   *
   * Read more in our [docs](https://www.prisma.io/docs/reference/tools-and-interfaces/prisma-client/raw-database-access).
   */
  $queryRawUnsafe<T = unknown>(query: string, ...values: any[]): Prisma.PrismaPromise<T>;


  /**
   * Allows the running of a sequence of read/write operations that are guaranteed to either succeed or fail as a whole.
   * @example
   * ```
   * const [george, bob, alice] = await prisma.$transaction([
   *   prisma.user.create({ data: { name: 'George' } }),
   *   prisma.user.create({ data: { name: 'Bob' } }),
   *   prisma.user.create({ data: { name: 'Alice' } }),
   * ])
   * ```
   * 
   * Read more in our [docs](https://www.prisma.io/docs/concepts/components/prisma-client/transactions).
   */
  $transaction<P extends Prisma.PrismaPromise<any>[]>(arg: [...P], options?: { isolationLevel?: Prisma.TransactionIsolationLevel }): $Utils.JsPromise<runtime.Types.Utils.UnwrapTuple<P>>

  $transaction<R>(fn: (prisma: Omit<PrismaClient, runtime.ITXClientDenyList>) => $Utils.JsPromise<R>, options?: { maxWait?: number, timeout?: number, isolationLevel?: Prisma.TransactionIsolationLevel }): $Utils.JsPromise<R>


  $extends: $Extensions.ExtendsHook<"extends", Prisma.TypeMapCb<ClientOptions>, ExtArgs, $Utils.Call<Prisma.TypeMapCb<ClientOptions>, {
    extArgs: ExtArgs
  }>>

      /**
   * `prisma.user`: Exposes CRUD operations for the **User** model.
    * Example usage:
    * ```ts
    * // Fetch zero or more Users
    * const users = await prisma.user.findMany()
    * ```
    */
  get user(): Prisma.UserDelegate<ExtArgs, ClientOptions>;

  /**
   * `prisma.client`: Exposes CRUD operations for the **Client** model.
    * Example usage:
    * ```ts
    * // Fetch zero or more Clients
    * const clients = await prisma.client.findMany()
    * ```
    */
  get client(): Prisma.ClientDelegate<ExtArgs, ClientOptions>;

  /**
   * `prisma.detalii`: Exposes CRUD operations for the **Detalii** model.
    * Example usage:
    * ```ts
    * // Fetch zero or more Detaliis
    * const detaliis = await prisma.detalii.findMany()
    * ```
    */
  get detalii(): Prisma.DetaliiDelegate<ExtArgs, ClientOptions>;

  /**
   * `prisma.punctDeLucru`: Exposes CRUD operations for the **PunctDeLucru** model.
    * Example usage:
    * ```ts
    * // Fetch zero or more PunctDeLucrus
    * const punctDeLucrus = await prisma.punctDeLucru.findMany()
    * ```
    */
  get punctDeLucru(): Prisma.PunctDeLucruDelegate<ExtArgs, ClientOptions>;

  /**
   * `prisma.istoric`: Exposes CRUD operations for the **Istoric** model.
    * Example usage:
    * ```ts
    * // Fetch zero or more Istorics
    * const istorics = await prisma.istoric.findMany()
    * ```
    */
  get istoric(): Prisma.IstoricDelegate<ExtArgs, ClientOptions>;

  /**
   * `prisma.task`: Exposes CRUD operations for the **Task** model.
    * Example usage:
    * ```ts
    * // Fetch zero or more Tasks
    * const tasks = await prisma.task.findMany()
    * ```
    */
  get task(): Prisma.TaskDelegate<ExtArgs, ClientOptions>;

  /**
   * `prisma.rule`: Exposes CRUD operations for the **Rule** model.
    * Example usage:
    * ```ts
    * // Fetch zero or more Rules
    * const rules = await prisma.rule.findMany()
    * ```
    */
  get rule(): Prisma.RuleDelegate<ExtArgs, ClientOptions>;

  /**
   * `prisma.ruleCondition`: Exposes CRUD operations for the **RuleCondition** model.
    * Example usage:
    * ```ts
    * // Fetch zero or more RuleConditions
    * const ruleConditions = await prisma.ruleCondition.findMany()
    * ```
    */
  get ruleCondition(): Prisma.RuleConditionDelegate<ExtArgs, ClientOptions>;

  /**
   * `prisma.userClient`: Exposes CRUD operations for the **UserClient** model.
    * Example usage:
    * ```ts
    * // Fetch zero or more UserClients
    * const userClients = await prisma.userClient.findMany()
    * ```
    */
  get userClient(): Prisma.UserClientDelegate<ExtArgs, ClientOptions>;
}

export namespace Prisma {
  export import DMMF = runtime.DMMF

  export type PrismaPromise<T> = $Public.PrismaPromise<T>

  /**
   * Validator
   */
  export import validator = runtime.Public.validator

  /**
   * Prisma Errors
   */
  export import PrismaClientKnownRequestError = runtime.PrismaClientKnownRequestError
  export import PrismaClientUnknownRequestError = runtime.PrismaClientUnknownRequestError
  export import PrismaClientRustPanicError = runtime.PrismaClientRustPanicError
  export import PrismaClientInitializationError = runtime.PrismaClientInitializationError
  export import PrismaClientValidationError = runtime.PrismaClientValidationError

  /**
   * Re-export of sql-template-tag
   */
  export import sql = runtime.sqltag
  export import empty = runtime.empty
  export import join = runtime.join
  export import raw = runtime.raw
  export import Sql = runtime.Sql



  /**
   * Decimal.js
   */
  export import Decimal = runtime.Decimal

  export type DecimalJsLike = runtime.DecimalJsLike

  /**
   * Metrics
   */
  export type Metrics = runtime.Metrics
  export type Metric<T> = runtime.Metric<T>
  export type MetricHistogram = runtime.MetricHistogram
  export type MetricHistogramBucket = runtime.MetricHistogramBucket

  /**
  * Extensions
  */
  export import Extension = $Extensions.UserArgs
  export import getExtensionContext = runtime.Extensions.getExtensionContext
  export import Args = $Public.Args
  export import Payload = $Public.Payload
  export import Result = $Public.Result
  export import Exact = $Public.Exact

  /**
   * Prisma Client JS version: 6.18.0
   * Query Engine version: 34b5a692b7bd79939a9a2c3ef97d816e749cda2f
   */
  export type PrismaVersion = {
    client: string
  }

  export const prismaVersion: PrismaVersion

  /**
   * Utility Types
   */


  export import Bytes = runtime.Bytes
  export import JsonObject = runtime.JsonObject
  export import JsonArray = runtime.JsonArray
  export import JsonValue = runtime.JsonValue
  export import InputJsonObject = runtime.InputJsonObject
  export import InputJsonArray = runtime.InputJsonArray
  export import InputJsonValue = runtime.InputJsonValue

  /**
   * Types of the values used to represent different kinds of `null` values when working with JSON fields.
   *
   * @see https://www.prisma.io/docs/concepts/components/prisma-client/working-with-fields/working-with-json-fields#filtering-on-a-json-field
   */
  namespace NullTypes {
    /**
    * Type of `Prisma.DbNull`.
    *
    * You cannot use other instances of this class. Please use the `Prisma.DbNull` value.
    *
    * @see https://www.prisma.io/docs/concepts/components/prisma-client/working-with-fields/working-with-json-fields#filtering-on-a-json-field
    */
    class DbNull {
      private DbNull: never
      private constructor()
    }

    /**
    * Type of `Prisma.JsonNull`.
    *
    * You cannot use other instances of this class. Please use the `Prisma.JsonNull` value.
    *
    * @see https://www.prisma.io/docs/concepts/components/prisma-client/working-with-fields/working-with-json-fields#filtering-on-a-json-field
    */
    class JsonNull {
      private JsonNull: never
      private constructor()
    }

    /**
    * Type of `Prisma.AnyNull`.
    *
    * You cannot use other instances of this class. Please use the `Prisma.AnyNull` value.
    *
    * @see https://www.prisma.io/docs/concepts/components/prisma-client/working-with-fields/working-with-json-fields#filtering-on-a-json-field
    */
    class AnyNull {
      private AnyNull: never
      private constructor()
    }
  }

  /**
   * Helper for filtering JSON entries that have `null` on the database (empty on the db)
   *
   * @see https://www.prisma.io/docs/concepts/components/prisma-client/working-with-fields/working-with-json-fields#filtering-on-a-json-field
   */
  export const DbNull: NullTypes.DbNull

  /**
   * Helper for filtering JSON entries that have JSON `null` values (not empty on the db)
   *
   * @see https://www.prisma.io/docs/concepts/components/prisma-client/working-with-fields/working-with-json-fields#filtering-on-a-json-field
   */
  export const JsonNull: NullTypes.JsonNull

  /**
   * Helper for filtering JSON entries that are `Prisma.DbNull` or `Prisma.JsonNull`
   *
   * @see https://www.prisma.io/docs/concepts/components/prisma-client/working-with-fields/working-with-json-fields#filtering-on-a-json-field
   */
  export const AnyNull: NullTypes.AnyNull

  type SelectAndInclude = {
    select: any
    include: any
  }

  type SelectAndOmit = {
    select: any
    omit: any
  }

  /**
   * Get the type of the value, that the Promise holds.
   */
  export type PromiseType<T extends PromiseLike<any>> = T extends PromiseLike<infer U> ? U : T;

  /**
   * Get the return type of a function which returns a Promise.
   */
  export type PromiseReturnType<T extends (...args: any) => $Utils.JsPromise<any>> = PromiseType<ReturnType<T>>

  /**
   * From T, pick a set of properties whose keys are in the union K
   */
  type Prisma__Pick<T, K extends keyof T> = {
      [P in K]: T[P];
  };


  export type Enumerable<T> = T | Array<T>;

  export type RequiredKeys<T> = {
    [K in keyof T]-?: {} extends Prisma__Pick<T, K> ? never : K
  }[keyof T]

  export type TruthyKeys<T> = keyof {
    [K in keyof T as T[K] extends false | undefined | null ? never : K]: K
  }

  export type TrueKeys<T> = TruthyKeys<Prisma__Pick<T, RequiredKeys<T>>>

  /**
   * Subset
   * @desc From `T` pick properties that exist in `U`. Simple version of Intersection
   */
  export type Subset<T, U> = {
    [key in keyof T]: key extends keyof U ? T[key] : never;
  };

  /**
   * SelectSubset
   * @desc From `T` pick properties that exist in `U`. Simple version of Intersection.
   * Additionally, it validates, if both select and include are present. If the case, it errors.
   */
  export type SelectSubset<T, U> = {
    [key in keyof T]: key extends keyof U ? T[key] : never
  } &
    (T extends SelectAndInclude
      ? 'Please either choose `select` or `include`.'
      : T extends SelectAndOmit
        ? 'Please either choose `select` or `omit`.'
        : {})

  /**
   * Subset + Intersection
   * @desc From `T` pick properties that exist in `U` and intersect `K`
   */
  export type SubsetIntersection<T, U, K> = {
    [key in keyof T]: key extends keyof U ? T[key] : never
  } &
    K

  type Without<T, U> = { [P in Exclude<keyof T, keyof U>]?: never };

  /**
   * XOR is needed to have a real mutually exclusive union type
   * https://stackoverflow.com/questions/42123407/does-typescript-support-mutually-exclusive-types
   */
  type XOR<T, U> =
    T extends object ?
    U extends object ?
      (Without<T, U> & U) | (Without<U, T> & T)
    : U : T


  /**
   * Is T a Record?
   */
  type IsObject<T extends any> = T extends Array<any>
  ? False
  : T extends Date
  ? False
  : T extends Uint8Array
  ? False
  : T extends BigInt
  ? False
  : T extends object
  ? True
  : False


  /**
   * If it's T[], return T
   */
  export type UnEnumerate<T extends unknown> = T extends Array<infer U> ? U : T

  /**
   * From ts-toolbelt
   */

  type __Either<O extends object, K extends Key> = Omit<O, K> &
    {
      // Merge all but K
      [P in K]: Prisma__Pick<O, P & keyof O> // With K possibilities
    }[K]

  type EitherStrict<O extends object, K extends Key> = Strict<__Either<O, K>>

  type EitherLoose<O extends object, K extends Key> = ComputeRaw<__Either<O, K>>

  type _Either<
    O extends object,
    K extends Key,
    strict extends Boolean
  > = {
    1: EitherStrict<O, K>
    0: EitherLoose<O, K>
  }[strict]

  type Either<
    O extends object,
    K extends Key,
    strict extends Boolean = 1
  > = O extends unknown ? _Either<O, K, strict> : never

  export type Union = any

  type PatchUndefined<O extends object, O1 extends object> = {
    [K in keyof O]: O[K] extends undefined ? At<O1, K> : O[K]
  } & {}

  /** Helper Types for "Merge" **/
  export type IntersectOf<U extends Union> = (
    U extends unknown ? (k: U) => void : never
  ) extends (k: infer I) => void
    ? I
    : never

  export type Overwrite<O extends object, O1 extends object> = {
      [K in keyof O]: K extends keyof O1 ? O1[K] : O[K];
  } & {};

  type _Merge<U extends object> = IntersectOf<Overwrite<U, {
      [K in keyof U]-?: At<U, K>;
  }>>;

  type Key = string | number | symbol;
  type AtBasic<O extends object, K extends Key> = K extends keyof O ? O[K] : never;
  type AtStrict<O extends object, K extends Key> = O[K & keyof O];
  type AtLoose<O extends object, K extends Key> = O extends unknown ? AtStrict<O, K> : never;
  export type At<O extends object, K extends Key, strict extends Boolean = 1> = {
      1: AtStrict<O, K>;
      0: AtLoose<O, K>;
  }[strict];

  export type ComputeRaw<A extends any> = A extends Function ? A : {
    [K in keyof A]: A[K];
  } & {};

  export type OptionalFlat<O> = {
    [K in keyof O]?: O[K];
  } & {};

  type _Record<K extends keyof any, T> = {
    [P in K]: T;
  };

  // cause typescript not to expand types and preserve names
  type NoExpand<T> = T extends unknown ? T : never;

  // this type assumes the passed object is entirely optional
  type AtLeast<O extends object, K extends string> = NoExpand<
    O extends unknown
    ? | (K extends keyof O ? { [P in K]: O[P] } & O : O)
      | {[P in keyof O as P extends K ? P : never]-?: O[P]} & O
    : never>;

  type _Strict<U, _U = U> = U extends unknown ? U & OptionalFlat<_Record<Exclude<Keys<_U>, keyof U>, never>> : never;

  export type Strict<U extends object> = ComputeRaw<_Strict<U>>;
  /** End Helper Types for "Merge" **/

  export type Merge<U extends object> = ComputeRaw<_Merge<Strict<U>>>;

  /**
  A [[Boolean]]
  */
  export type Boolean = True | False

  // /**
  // 1
  // */
  export type True = 1

  /**
  0
  */
  export type False = 0

  export type Not<B extends Boolean> = {
    0: 1
    1: 0
  }[B]

  export type Extends<A1 extends any, A2 extends any> = [A1] extends [never]
    ? 0 // anything `never` is false
    : A1 extends A2
    ? 1
    : 0

  export type Has<U extends Union, U1 extends Union> = Not<
    Extends<Exclude<U1, U>, U1>
  >

  export type Or<B1 extends Boolean, B2 extends Boolean> = {
    0: {
      0: 0
      1: 1
    }
    1: {
      0: 1
      1: 1
    }
  }[B1][B2]

  export type Keys<U extends Union> = U extends unknown ? keyof U : never

  type Cast<A, B> = A extends B ? A : B;

  export const type: unique symbol;



  /**
   * Used by group by
   */

  export type GetScalarType<T, O> = O extends object ? {
    [P in keyof T]: P extends keyof O
      ? O[P]
      : never
  } : never

  type FieldPaths<
    T,
    U = Omit<T, '_avg' | '_sum' | '_count' | '_min' | '_max'>
  > = IsObject<T> extends True ? U : T

  type GetHavingFields<T> = {
    [K in keyof T]: Or<
      Or<Extends<'OR', K>, Extends<'AND', K>>,
      Extends<'NOT', K>
    > extends True
      ? // infer is only needed to not hit TS limit
        // based on the brilliant idea of Pierre-Antoine Mills
        // https://github.com/microsoft/TypeScript/issues/30188#issuecomment-478938437
        T[K] extends infer TK
        ? GetHavingFields<UnEnumerate<TK> extends object ? Merge<UnEnumerate<TK>> : never>
        : never
      : {} extends FieldPaths<T[K]>
      ? never
      : K
  }[keyof T]

  /**
   * Convert tuple to union
   */
  type _TupleToUnion<T> = T extends (infer E)[] ? E : never
  type TupleToUnion<K extends readonly any[]> = _TupleToUnion<K>
  type MaybeTupleToUnion<T> = T extends any[] ? TupleToUnion<T> : T

  /**
   * Like `Pick`, but additionally can also accept an array of keys
   */
  type PickEnumerable<T, K extends Enumerable<keyof T> | keyof T> = Prisma__Pick<T, MaybeTupleToUnion<K>>

  /**
   * Exclude all keys with underscores
   */
  type ExcludeUnderscoreKeys<T extends string> = T extends `_${string}` ? never : T


  export type FieldRef<Model, FieldType> = runtime.FieldRef<Model, FieldType>

  type FieldRefInputType<Model, FieldType> = Model extends never ? never : FieldRef<Model, FieldType>


  export const ModelName: {
    User: 'User',
    Client: 'Client',
    Detalii: 'Detalii',
    PunctDeLucru: 'PunctDeLucru',
    Istoric: 'Istoric',
    Task: 'Task',
    Rule: 'Rule',
    RuleCondition: 'RuleCondition',
    UserClient: 'UserClient'
  };

  export type ModelName = (typeof ModelName)[keyof typeof ModelName]


  export type Datasources = {
    db?: Datasource
  }

  interface TypeMapCb<ClientOptions = {}> extends $Utils.Fn<{extArgs: $Extensions.InternalArgs }, $Utils.Record<string, any>> {
    returns: Prisma.TypeMap<this['params']['extArgs'], ClientOptions extends { omit: infer OmitOptions } ? OmitOptions : {}>
  }

  export type TypeMap<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs, GlobalOmitOptions = {}> = {
    globalOmitOptions: {
      omit: GlobalOmitOptions
    }
    meta: {
      modelProps: "user" | "client" | "detalii" | "punctDeLucru" | "istoric" | "task" | "rule" | "ruleCondition" | "userClient"
      txIsolationLevel: Prisma.TransactionIsolationLevel
    }
    model: {
      User: {
        payload: Prisma.$UserPayload<ExtArgs>
        fields: Prisma.UserFieldRefs
        operations: {
          findUnique: {
            args: Prisma.UserFindUniqueArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserPayload> | null
          }
          findUniqueOrThrow: {
            args: Prisma.UserFindUniqueOrThrowArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserPayload>
          }
          findFirst: {
            args: Prisma.UserFindFirstArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserPayload> | null
          }
          findFirstOrThrow: {
            args: Prisma.UserFindFirstOrThrowArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserPayload>
          }
          findMany: {
            args: Prisma.UserFindManyArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserPayload>[]
          }
          create: {
            args: Prisma.UserCreateArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserPayload>
          }
          createMany: {
            args: Prisma.UserCreateManyArgs<ExtArgs>
            result: BatchPayload
          }
          createManyAndReturn: {
            args: Prisma.UserCreateManyAndReturnArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserPayload>[]
          }
          delete: {
            args: Prisma.UserDeleteArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserPayload>
          }
          update: {
            args: Prisma.UserUpdateArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserPayload>
          }
          deleteMany: {
            args: Prisma.UserDeleteManyArgs<ExtArgs>
            result: BatchPayload
          }
          updateMany: {
            args: Prisma.UserUpdateManyArgs<ExtArgs>
            result: BatchPayload
          }
          updateManyAndReturn: {
            args: Prisma.UserUpdateManyAndReturnArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserPayload>[]
          }
          upsert: {
            args: Prisma.UserUpsertArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserPayload>
          }
          aggregate: {
            args: Prisma.UserAggregateArgs<ExtArgs>
            result: $Utils.Optional<AggregateUser>
          }
          groupBy: {
            args: Prisma.UserGroupByArgs<ExtArgs>
            result: $Utils.Optional<UserGroupByOutputType>[]
          }
          count: {
            args: Prisma.UserCountArgs<ExtArgs>
            result: $Utils.Optional<UserCountAggregateOutputType> | number
          }
        }
      }
      Client: {
        payload: Prisma.$ClientPayload<ExtArgs>
        fields: Prisma.ClientFieldRefs
        operations: {
          findUnique: {
            args: Prisma.ClientFindUniqueArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$ClientPayload> | null
          }
          findUniqueOrThrow: {
            args: Prisma.ClientFindUniqueOrThrowArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$ClientPayload>
          }
          findFirst: {
            args: Prisma.ClientFindFirstArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$ClientPayload> | null
          }
          findFirstOrThrow: {
            args: Prisma.ClientFindFirstOrThrowArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$ClientPayload>
          }
          findMany: {
            args: Prisma.ClientFindManyArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$ClientPayload>[]
          }
          create: {
            args: Prisma.ClientCreateArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$ClientPayload>
          }
          createMany: {
            args: Prisma.ClientCreateManyArgs<ExtArgs>
            result: BatchPayload
          }
          createManyAndReturn: {
            args: Prisma.ClientCreateManyAndReturnArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$ClientPayload>[]
          }
          delete: {
            args: Prisma.ClientDeleteArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$ClientPayload>
          }
          update: {
            args: Prisma.ClientUpdateArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$ClientPayload>
          }
          deleteMany: {
            args: Prisma.ClientDeleteManyArgs<ExtArgs>
            result: BatchPayload
          }
          updateMany: {
            args: Prisma.ClientUpdateManyArgs<ExtArgs>
            result: BatchPayload
          }
          updateManyAndReturn: {
            args: Prisma.ClientUpdateManyAndReturnArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$ClientPayload>[]
          }
          upsert: {
            args: Prisma.ClientUpsertArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$ClientPayload>
          }
          aggregate: {
            args: Prisma.ClientAggregateArgs<ExtArgs>
            result: $Utils.Optional<AggregateClient>
          }
          groupBy: {
            args: Prisma.ClientGroupByArgs<ExtArgs>
            result: $Utils.Optional<ClientGroupByOutputType>[]
          }
          count: {
            args: Prisma.ClientCountArgs<ExtArgs>
            result: $Utils.Optional<ClientCountAggregateOutputType> | number
          }
        }
      }
      Detalii: {
        payload: Prisma.$DetaliiPayload<ExtArgs>
        fields: Prisma.DetaliiFieldRefs
        operations: {
          findUnique: {
            args: Prisma.DetaliiFindUniqueArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$DetaliiPayload> | null
          }
          findUniqueOrThrow: {
            args: Prisma.DetaliiFindUniqueOrThrowArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$DetaliiPayload>
          }
          findFirst: {
            args: Prisma.DetaliiFindFirstArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$DetaliiPayload> | null
          }
          findFirstOrThrow: {
            args: Prisma.DetaliiFindFirstOrThrowArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$DetaliiPayload>
          }
          findMany: {
            args: Prisma.DetaliiFindManyArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$DetaliiPayload>[]
          }
          create: {
            args: Prisma.DetaliiCreateArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$DetaliiPayload>
          }
          createMany: {
            args: Prisma.DetaliiCreateManyArgs<ExtArgs>
            result: BatchPayload
          }
          createManyAndReturn: {
            args: Prisma.DetaliiCreateManyAndReturnArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$DetaliiPayload>[]
          }
          delete: {
            args: Prisma.DetaliiDeleteArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$DetaliiPayload>
          }
          update: {
            args: Prisma.DetaliiUpdateArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$DetaliiPayload>
          }
          deleteMany: {
            args: Prisma.DetaliiDeleteManyArgs<ExtArgs>
            result: BatchPayload
          }
          updateMany: {
            args: Prisma.DetaliiUpdateManyArgs<ExtArgs>
            result: BatchPayload
          }
          updateManyAndReturn: {
            args: Prisma.DetaliiUpdateManyAndReturnArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$DetaliiPayload>[]
          }
          upsert: {
            args: Prisma.DetaliiUpsertArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$DetaliiPayload>
          }
          aggregate: {
            args: Prisma.DetaliiAggregateArgs<ExtArgs>
            result: $Utils.Optional<AggregateDetalii>
          }
          groupBy: {
            args: Prisma.DetaliiGroupByArgs<ExtArgs>
            result: $Utils.Optional<DetaliiGroupByOutputType>[]
          }
          count: {
            args: Prisma.DetaliiCountArgs<ExtArgs>
            result: $Utils.Optional<DetaliiCountAggregateOutputType> | number
          }
        }
      }
      PunctDeLucru: {
        payload: Prisma.$PunctDeLucruPayload<ExtArgs>
        fields: Prisma.PunctDeLucruFieldRefs
        operations: {
          findUnique: {
            args: Prisma.PunctDeLucruFindUniqueArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$PunctDeLucruPayload> | null
          }
          findUniqueOrThrow: {
            args: Prisma.PunctDeLucruFindUniqueOrThrowArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$PunctDeLucruPayload>
          }
          findFirst: {
            args: Prisma.PunctDeLucruFindFirstArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$PunctDeLucruPayload> | null
          }
          findFirstOrThrow: {
            args: Prisma.PunctDeLucruFindFirstOrThrowArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$PunctDeLucruPayload>
          }
          findMany: {
            args: Prisma.PunctDeLucruFindManyArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$PunctDeLucruPayload>[]
          }
          create: {
            args: Prisma.PunctDeLucruCreateArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$PunctDeLucruPayload>
          }
          createMany: {
            args: Prisma.PunctDeLucruCreateManyArgs<ExtArgs>
            result: BatchPayload
          }
          createManyAndReturn: {
            args: Prisma.PunctDeLucruCreateManyAndReturnArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$PunctDeLucruPayload>[]
          }
          delete: {
            args: Prisma.PunctDeLucruDeleteArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$PunctDeLucruPayload>
          }
          update: {
            args: Prisma.PunctDeLucruUpdateArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$PunctDeLucruPayload>
          }
          deleteMany: {
            args: Prisma.PunctDeLucruDeleteManyArgs<ExtArgs>
            result: BatchPayload
          }
          updateMany: {
            args: Prisma.PunctDeLucruUpdateManyArgs<ExtArgs>
            result: BatchPayload
          }
          updateManyAndReturn: {
            args: Prisma.PunctDeLucruUpdateManyAndReturnArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$PunctDeLucruPayload>[]
          }
          upsert: {
            args: Prisma.PunctDeLucruUpsertArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$PunctDeLucruPayload>
          }
          aggregate: {
            args: Prisma.PunctDeLucruAggregateArgs<ExtArgs>
            result: $Utils.Optional<AggregatePunctDeLucru>
          }
          groupBy: {
            args: Prisma.PunctDeLucruGroupByArgs<ExtArgs>
            result: $Utils.Optional<PunctDeLucruGroupByOutputType>[]
          }
          count: {
            args: Prisma.PunctDeLucruCountArgs<ExtArgs>
            result: $Utils.Optional<PunctDeLucruCountAggregateOutputType> | number
          }
        }
      }
      Istoric: {
        payload: Prisma.$IstoricPayload<ExtArgs>
        fields: Prisma.IstoricFieldRefs
        operations: {
          findUnique: {
            args: Prisma.IstoricFindUniqueArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$IstoricPayload> | null
          }
          findUniqueOrThrow: {
            args: Prisma.IstoricFindUniqueOrThrowArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$IstoricPayload>
          }
          findFirst: {
            args: Prisma.IstoricFindFirstArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$IstoricPayload> | null
          }
          findFirstOrThrow: {
            args: Prisma.IstoricFindFirstOrThrowArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$IstoricPayload>
          }
          findMany: {
            args: Prisma.IstoricFindManyArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$IstoricPayload>[]
          }
          create: {
            args: Prisma.IstoricCreateArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$IstoricPayload>
          }
          createMany: {
            args: Prisma.IstoricCreateManyArgs<ExtArgs>
            result: BatchPayload
          }
          createManyAndReturn: {
            args: Prisma.IstoricCreateManyAndReturnArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$IstoricPayload>[]
          }
          delete: {
            args: Prisma.IstoricDeleteArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$IstoricPayload>
          }
          update: {
            args: Prisma.IstoricUpdateArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$IstoricPayload>
          }
          deleteMany: {
            args: Prisma.IstoricDeleteManyArgs<ExtArgs>
            result: BatchPayload
          }
          updateMany: {
            args: Prisma.IstoricUpdateManyArgs<ExtArgs>
            result: BatchPayload
          }
          updateManyAndReturn: {
            args: Prisma.IstoricUpdateManyAndReturnArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$IstoricPayload>[]
          }
          upsert: {
            args: Prisma.IstoricUpsertArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$IstoricPayload>
          }
          aggregate: {
            args: Prisma.IstoricAggregateArgs<ExtArgs>
            result: $Utils.Optional<AggregateIstoric>
          }
          groupBy: {
            args: Prisma.IstoricGroupByArgs<ExtArgs>
            result: $Utils.Optional<IstoricGroupByOutputType>[]
          }
          count: {
            args: Prisma.IstoricCountArgs<ExtArgs>
            result: $Utils.Optional<IstoricCountAggregateOutputType> | number
          }
        }
      }
      Task: {
        payload: Prisma.$TaskPayload<ExtArgs>
        fields: Prisma.TaskFieldRefs
        operations: {
          findUnique: {
            args: Prisma.TaskFindUniqueArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$TaskPayload> | null
          }
          findUniqueOrThrow: {
            args: Prisma.TaskFindUniqueOrThrowArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$TaskPayload>
          }
          findFirst: {
            args: Prisma.TaskFindFirstArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$TaskPayload> | null
          }
          findFirstOrThrow: {
            args: Prisma.TaskFindFirstOrThrowArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$TaskPayload>
          }
          findMany: {
            args: Prisma.TaskFindManyArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$TaskPayload>[]
          }
          create: {
            args: Prisma.TaskCreateArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$TaskPayload>
          }
          createMany: {
            args: Prisma.TaskCreateManyArgs<ExtArgs>
            result: BatchPayload
          }
          createManyAndReturn: {
            args: Prisma.TaskCreateManyAndReturnArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$TaskPayload>[]
          }
          delete: {
            args: Prisma.TaskDeleteArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$TaskPayload>
          }
          update: {
            args: Prisma.TaskUpdateArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$TaskPayload>
          }
          deleteMany: {
            args: Prisma.TaskDeleteManyArgs<ExtArgs>
            result: BatchPayload
          }
          updateMany: {
            args: Prisma.TaskUpdateManyArgs<ExtArgs>
            result: BatchPayload
          }
          updateManyAndReturn: {
            args: Prisma.TaskUpdateManyAndReturnArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$TaskPayload>[]
          }
          upsert: {
            args: Prisma.TaskUpsertArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$TaskPayload>
          }
          aggregate: {
            args: Prisma.TaskAggregateArgs<ExtArgs>
            result: $Utils.Optional<AggregateTask>
          }
          groupBy: {
            args: Prisma.TaskGroupByArgs<ExtArgs>
            result: $Utils.Optional<TaskGroupByOutputType>[]
          }
          count: {
            args: Prisma.TaskCountArgs<ExtArgs>
            result: $Utils.Optional<TaskCountAggregateOutputType> | number
          }
        }
      }
      Rule: {
        payload: Prisma.$RulePayload<ExtArgs>
        fields: Prisma.RuleFieldRefs
        operations: {
          findUnique: {
            args: Prisma.RuleFindUniqueArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RulePayload> | null
          }
          findUniqueOrThrow: {
            args: Prisma.RuleFindUniqueOrThrowArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RulePayload>
          }
          findFirst: {
            args: Prisma.RuleFindFirstArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RulePayload> | null
          }
          findFirstOrThrow: {
            args: Prisma.RuleFindFirstOrThrowArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RulePayload>
          }
          findMany: {
            args: Prisma.RuleFindManyArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RulePayload>[]
          }
          create: {
            args: Prisma.RuleCreateArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RulePayload>
          }
          createMany: {
            args: Prisma.RuleCreateManyArgs<ExtArgs>
            result: BatchPayload
          }
          createManyAndReturn: {
            args: Prisma.RuleCreateManyAndReturnArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RulePayload>[]
          }
          delete: {
            args: Prisma.RuleDeleteArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RulePayload>
          }
          update: {
            args: Prisma.RuleUpdateArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RulePayload>
          }
          deleteMany: {
            args: Prisma.RuleDeleteManyArgs<ExtArgs>
            result: BatchPayload
          }
          updateMany: {
            args: Prisma.RuleUpdateManyArgs<ExtArgs>
            result: BatchPayload
          }
          updateManyAndReturn: {
            args: Prisma.RuleUpdateManyAndReturnArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RulePayload>[]
          }
          upsert: {
            args: Prisma.RuleUpsertArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RulePayload>
          }
          aggregate: {
            args: Prisma.RuleAggregateArgs<ExtArgs>
            result: $Utils.Optional<AggregateRule>
          }
          groupBy: {
            args: Prisma.RuleGroupByArgs<ExtArgs>
            result: $Utils.Optional<RuleGroupByOutputType>[]
          }
          count: {
            args: Prisma.RuleCountArgs<ExtArgs>
            result: $Utils.Optional<RuleCountAggregateOutputType> | number
          }
        }
      }
      RuleCondition: {
        payload: Prisma.$RuleConditionPayload<ExtArgs>
        fields: Prisma.RuleConditionFieldRefs
        operations: {
          findUnique: {
            args: Prisma.RuleConditionFindUniqueArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RuleConditionPayload> | null
          }
          findUniqueOrThrow: {
            args: Prisma.RuleConditionFindUniqueOrThrowArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RuleConditionPayload>
          }
          findFirst: {
            args: Prisma.RuleConditionFindFirstArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RuleConditionPayload> | null
          }
          findFirstOrThrow: {
            args: Prisma.RuleConditionFindFirstOrThrowArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RuleConditionPayload>
          }
          findMany: {
            args: Prisma.RuleConditionFindManyArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RuleConditionPayload>[]
          }
          create: {
            args: Prisma.RuleConditionCreateArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RuleConditionPayload>
          }
          createMany: {
            args: Prisma.RuleConditionCreateManyArgs<ExtArgs>
            result: BatchPayload
          }
          createManyAndReturn: {
            args: Prisma.RuleConditionCreateManyAndReturnArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RuleConditionPayload>[]
          }
          delete: {
            args: Prisma.RuleConditionDeleteArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RuleConditionPayload>
          }
          update: {
            args: Prisma.RuleConditionUpdateArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RuleConditionPayload>
          }
          deleteMany: {
            args: Prisma.RuleConditionDeleteManyArgs<ExtArgs>
            result: BatchPayload
          }
          updateMany: {
            args: Prisma.RuleConditionUpdateManyArgs<ExtArgs>
            result: BatchPayload
          }
          updateManyAndReturn: {
            args: Prisma.RuleConditionUpdateManyAndReturnArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RuleConditionPayload>[]
          }
          upsert: {
            args: Prisma.RuleConditionUpsertArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$RuleConditionPayload>
          }
          aggregate: {
            args: Prisma.RuleConditionAggregateArgs<ExtArgs>
            result: $Utils.Optional<AggregateRuleCondition>
          }
          groupBy: {
            args: Prisma.RuleConditionGroupByArgs<ExtArgs>
            result: $Utils.Optional<RuleConditionGroupByOutputType>[]
          }
          count: {
            args: Prisma.RuleConditionCountArgs<ExtArgs>
            result: $Utils.Optional<RuleConditionCountAggregateOutputType> | number
          }
        }
      }
      UserClient: {
        payload: Prisma.$UserClientPayload<ExtArgs>
        fields: Prisma.UserClientFieldRefs
        operations: {
          findUnique: {
            args: Prisma.UserClientFindUniqueArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserClientPayload> | null
          }
          findUniqueOrThrow: {
            args: Prisma.UserClientFindUniqueOrThrowArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserClientPayload>
          }
          findFirst: {
            args: Prisma.UserClientFindFirstArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserClientPayload> | null
          }
          findFirstOrThrow: {
            args: Prisma.UserClientFindFirstOrThrowArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserClientPayload>
          }
          findMany: {
            args: Prisma.UserClientFindManyArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserClientPayload>[]
          }
          create: {
            args: Prisma.UserClientCreateArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserClientPayload>
          }
          createMany: {
            args: Prisma.UserClientCreateManyArgs<ExtArgs>
            result: BatchPayload
          }
          createManyAndReturn: {
            args: Prisma.UserClientCreateManyAndReturnArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserClientPayload>[]
          }
          delete: {
            args: Prisma.UserClientDeleteArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserClientPayload>
          }
          update: {
            args: Prisma.UserClientUpdateArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserClientPayload>
          }
          deleteMany: {
            args: Prisma.UserClientDeleteManyArgs<ExtArgs>
            result: BatchPayload
          }
          updateMany: {
            args: Prisma.UserClientUpdateManyArgs<ExtArgs>
            result: BatchPayload
          }
          updateManyAndReturn: {
            args: Prisma.UserClientUpdateManyAndReturnArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserClientPayload>[]
          }
          upsert: {
            args: Prisma.UserClientUpsertArgs<ExtArgs>
            result: $Utils.PayloadToResult<Prisma.$UserClientPayload>
          }
          aggregate: {
            args: Prisma.UserClientAggregateArgs<ExtArgs>
            result: $Utils.Optional<AggregateUserClient>
          }
          groupBy: {
            args: Prisma.UserClientGroupByArgs<ExtArgs>
            result: $Utils.Optional<UserClientGroupByOutputType>[]
          }
          count: {
            args: Prisma.UserClientCountArgs<ExtArgs>
            result: $Utils.Optional<UserClientCountAggregateOutputType> | number
          }
        }
      }
    }
  } & {
    other: {
      payload: any
      operations: {
        $executeRaw: {
          args: [query: TemplateStringsArray | Prisma.Sql, ...values: any[]],
          result: any
        }
        $executeRawUnsafe: {
          args: [query: string, ...values: any[]],
          result: any
        }
        $queryRaw: {
          args: [query: TemplateStringsArray | Prisma.Sql, ...values: any[]],
          result: any
        }
        $queryRawUnsafe: {
          args: [query: string, ...values: any[]],
          result: any
        }
      }
    }
  }
  export const defineExtension: $Extensions.ExtendsHook<"define", Prisma.TypeMapCb, $Extensions.DefaultArgs>
  export type DefaultPrismaClient = PrismaClient
  export type ErrorFormat = 'pretty' | 'colorless' | 'minimal'
  export interface PrismaClientOptions {
    /**
     * Overwrites the datasource url from your schema.prisma file
     */
    datasources?: Datasources
    /**
     * Overwrites the datasource url from your schema.prisma file
     */
    datasourceUrl?: string
    /**
     * @default "colorless"
     */
    errorFormat?: ErrorFormat
    /**
     * @example
     * ```
     * // Shorthand for `emit: 'stdout'`
     * log: ['query', 'info', 'warn', 'error']
     * 
     * // Emit as events only
     * log: [
     *   { emit: 'event', level: 'query' },
     *   { emit: 'event', level: 'info' },
     *   { emit: 'event', level: 'warn' }
     *   { emit: 'event', level: 'error' }
     * ]
     * 
     * / Emit as events and log to stdout
     * og: [
     *  { emit: 'stdout', level: 'query' },
     *  { emit: 'stdout', level: 'info' },
     *  { emit: 'stdout', level: 'warn' }
     *  { emit: 'stdout', level: 'error' }
     * 
     * ```
     * Read more in our [docs](https://www.prisma.io/docs/reference/tools-and-interfaces/prisma-client/logging#the-log-option).
     */
    log?: (LogLevel | LogDefinition)[]
    /**
     * The default values for transactionOptions
     * maxWait ?= 2000
     * timeout ?= 5000
     */
    transactionOptions?: {
      maxWait?: number
      timeout?: number
      isolationLevel?: Prisma.TransactionIsolationLevel
    }
    /**
     * Instance of a Driver Adapter, e.g., like one provided by `@prisma/adapter-planetscale`
     */
    adapter?: runtime.SqlDriverAdapterFactory | null
    /**
     * Global configuration for omitting model fields by default.
     * 
     * @example
     * ```
     * const prisma = new PrismaClient({
     *   omit: {
     *     user: {
     *       password: true
     *     }
     *   }
     * })
     * ```
     */
    omit?: Prisma.GlobalOmitConfig
  }
  export type GlobalOmitConfig = {
    user?: UserOmit
    client?: ClientOmit
    detalii?: DetaliiOmit
    punctDeLucru?: PunctDeLucruOmit
    istoric?: IstoricOmit
    task?: TaskOmit
    rule?: RuleOmit
    ruleCondition?: RuleConditionOmit
    userClient?: UserClientOmit
  }

  /* Types for Logging */
  export type LogLevel = 'info' | 'query' | 'warn' | 'error'
  export type LogDefinition = {
    level: LogLevel
    emit: 'stdout' | 'event'
  }

  export type CheckIsLogLevel<T> = T extends LogLevel ? T : never;

  export type GetLogType<T> = CheckIsLogLevel<
    T extends LogDefinition ? T['level'] : T
  >;

  export type GetEvents<T extends any[]> = T extends Array<LogLevel | LogDefinition>
    ? GetLogType<T[number]>
    : never;

  export type QueryEvent = {
    timestamp: Date
    query: string
    params: string
    duration: number
    target: string
  }

  export type LogEvent = {
    timestamp: Date
    message: string
    target: string
  }
  /* End Types for Logging */


  export type PrismaAction =
    | 'findUnique'
    | 'findUniqueOrThrow'
    | 'findMany'
    | 'findFirst'
    | 'findFirstOrThrow'
    | 'create'
    | 'createMany'
    | 'createManyAndReturn'
    | 'update'
    | 'updateMany'
    | 'updateManyAndReturn'
    | 'upsert'
    | 'delete'
    | 'deleteMany'
    | 'executeRaw'
    | 'queryRaw'
    | 'aggregate'
    | 'count'
    | 'runCommandRaw'
    | 'findRaw'
    | 'groupBy'

  // tested in getLogLevel.test.ts
  export function getLogLevel(log: Array<LogLevel | LogDefinition>): LogLevel | undefined;

  /**
   * `PrismaClient` proxy available in interactive transactions.
   */
  export type TransactionClient = Omit<Prisma.DefaultPrismaClient, runtime.ITXClientDenyList>

  export type Datasource = {
    url?: string
  }

  /**
   * Count Types
   */


  /**
   * Count Type UserCountOutputType
   */

  export type UserCountOutputType = {
    tasks: number
    clients: number
  }

  export type UserCountOutputTypeSelect<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    tasks?: boolean | UserCountOutputTypeCountTasksArgs
    clients?: boolean | UserCountOutputTypeCountClientsArgs
  }

  // Custom InputTypes
  /**
   * UserCountOutputType without action
   */
  export type UserCountOutputTypeDefaultArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the UserCountOutputType
     */
    select?: UserCountOutputTypeSelect<ExtArgs> | null
  }

  /**
   * UserCountOutputType without action
   */
  export type UserCountOutputTypeCountTasksArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    where?: TaskWhereInput
  }

  /**
   * UserCountOutputType without action
   */
  export type UserCountOutputTypeCountClientsArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    where?: UserClientWhereInput
  }


  /**
   * Count Type ClientCountOutputType
   */

  export type ClientCountOutputType = {
    puncteDeLucru: number
    istorice: number
    tasks: number
    users: number
  }

  export type ClientCountOutputTypeSelect<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    puncteDeLucru?: boolean | ClientCountOutputTypeCountPuncteDeLucruArgs
    istorice?: boolean | ClientCountOutputTypeCountIstoriceArgs
    tasks?: boolean | ClientCountOutputTypeCountTasksArgs
    users?: boolean | ClientCountOutputTypeCountUsersArgs
  }

  // Custom InputTypes
  /**
   * ClientCountOutputType without action
   */
  export type ClientCountOutputTypeDefaultArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the ClientCountOutputType
     */
    select?: ClientCountOutputTypeSelect<ExtArgs> | null
  }

  /**
   * ClientCountOutputType without action
   */
  export type ClientCountOutputTypeCountPuncteDeLucruArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    where?: PunctDeLucruWhereInput
  }

  /**
   * ClientCountOutputType without action
   */
  export type ClientCountOutputTypeCountIstoriceArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    where?: IstoricWhereInput
  }

  /**
   * ClientCountOutputType without action
   */
  export type ClientCountOutputTypeCountTasksArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    where?: TaskWhereInput
  }

  /**
   * ClientCountOutputType without action
   */
  export type ClientCountOutputTypeCountUsersArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    where?: UserClientWhereInput
  }


  /**
   * Count Type RuleCountOutputType
   */

  export type RuleCountOutputType = {
    conditions: number
  }

  export type RuleCountOutputTypeSelect<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    conditions?: boolean | RuleCountOutputTypeCountConditionsArgs
  }

  // Custom InputTypes
  /**
   * RuleCountOutputType without action
   */
  export type RuleCountOutputTypeDefaultArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the RuleCountOutputType
     */
    select?: RuleCountOutputTypeSelect<ExtArgs> | null
  }

  /**
   * RuleCountOutputType without action
   */
  export type RuleCountOutputTypeCountConditionsArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    where?: RuleConditionWhereInput
  }


  /**
   * Models
   */

  /**
   * Model User
   */

  export type AggregateUser = {
    _count: UserCountAggregateOutputType | null
    _avg: UserAvgAggregateOutputType | null
    _sum: UserSumAggregateOutputType | null
    _min: UserMinAggregateOutputType | null
    _max: UserMaxAggregateOutputType | null
  }

  export type UserAvgAggregateOutputType = {
    id: number | null
  }

  export type UserSumAggregateOutputType = {
    id: number | null
  }

  export type UserMinAggregateOutputType = {
    id: number | null
    email: string | null
    name: string | null
    password: string | null
    rol: $Enums.Role | null
  }

  export type UserMaxAggregateOutputType = {
    id: number | null
    email: string | null
    name: string | null
    password: string | null
    rol: $Enums.Role | null
  }

  export type UserCountAggregateOutputType = {
    id: number
    email: number
    name: number
    password: number
    rol: number
    _all: number
  }


  export type UserAvgAggregateInputType = {
    id?: true
  }

  export type UserSumAggregateInputType = {
    id?: true
  }

  export type UserMinAggregateInputType = {
    id?: true
    email?: true
    name?: true
    password?: true
    rol?: true
  }

  export type UserMaxAggregateInputType = {
    id?: true
    email?: true
    name?: true
    password?: true
    rol?: true
  }

  export type UserCountAggregateInputType = {
    id?: true
    email?: true
    name?: true
    password?: true
    rol?: true
    _all?: true
  }

  export type UserAggregateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Filter which User to aggregate.
     */
    where?: UserWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Users to fetch.
     */
    orderBy?: UserOrderByWithRelationInput | UserOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the start position
     */
    cursor?: UserWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Users from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Users.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Count returned Users
    **/
    _count?: true | UserCountAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to average
    **/
    _avg?: UserAvgAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to sum
    **/
    _sum?: UserSumAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to find the minimum value
    **/
    _min?: UserMinAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to find the maximum value
    **/
    _max?: UserMaxAggregateInputType
  }

  export type GetUserAggregateType<T extends UserAggregateArgs> = {
        [P in keyof T & keyof AggregateUser]: P extends '_count' | 'count'
      ? T[P] extends true
        ? number
        : GetScalarType<T[P], AggregateUser[P]>
      : GetScalarType<T[P], AggregateUser[P]>
  }




  export type UserGroupByArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    where?: UserWhereInput
    orderBy?: UserOrderByWithAggregationInput | UserOrderByWithAggregationInput[]
    by: UserScalarFieldEnum[] | UserScalarFieldEnum
    having?: UserScalarWhereWithAggregatesInput
    take?: number
    skip?: number
    _count?: UserCountAggregateInputType | true
    _avg?: UserAvgAggregateInputType
    _sum?: UserSumAggregateInputType
    _min?: UserMinAggregateInputType
    _max?: UserMaxAggregateInputType
  }

  export type UserGroupByOutputType = {
    id: number
    email: string
    name: string | null
    password: string | null
    rol: $Enums.Role
    _count: UserCountAggregateOutputType | null
    _avg: UserAvgAggregateOutputType | null
    _sum: UserSumAggregateOutputType | null
    _min: UserMinAggregateOutputType | null
    _max: UserMaxAggregateOutputType | null
  }

  type GetUserGroupByPayload<T extends UserGroupByArgs> = Prisma.PrismaPromise<
    Array<
      PickEnumerable<UserGroupByOutputType, T['by']> &
        {
          [P in ((keyof T) & (keyof UserGroupByOutputType))]: P extends '_count'
            ? T[P] extends boolean
              ? number
              : GetScalarType<T[P], UserGroupByOutputType[P]>
            : GetScalarType<T[P], UserGroupByOutputType[P]>
        }
      >
    >


  export type UserSelect<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    email?: boolean
    name?: boolean
    password?: boolean
    rol?: boolean
    tasks?: boolean | User$tasksArgs<ExtArgs>
    clients?: boolean | User$clientsArgs<ExtArgs>
    _count?: boolean | UserCountOutputTypeDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["user"]>

  export type UserSelectCreateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    email?: boolean
    name?: boolean
    password?: boolean
    rol?: boolean
  }, ExtArgs["result"]["user"]>

  export type UserSelectUpdateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    email?: boolean
    name?: boolean
    password?: boolean
    rol?: boolean
  }, ExtArgs["result"]["user"]>

  export type UserSelectScalar = {
    id?: boolean
    email?: boolean
    name?: boolean
    password?: boolean
    rol?: boolean
  }

  export type UserOmit<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetOmit<"id" | "email" | "name" | "password" | "rol", ExtArgs["result"]["user"]>
  export type UserInclude<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    tasks?: boolean | User$tasksArgs<ExtArgs>
    clients?: boolean | User$clientsArgs<ExtArgs>
    _count?: boolean | UserCountOutputTypeDefaultArgs<ExtArgs>
  }
  export type UserIncludeCreateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {}
  export type UserIncludeUpdateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {}

  export type $UserPayload<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    name: "User"
    objects: {
      tasks: Prisma.$TaskPayload<ExtArgs>[]
      clients: Prisma.$UserClientPayload<ExtArgs>[]
    }
    scalars: $Extensions.GetPayloadResult<{
      id: number
      email: string
      name: string | null
      password: string | null
      rol: $Enums.Role
    }, ExtArgs["result"]["user"]>
    composites: {}
  }

  type UserGetPayload<S extends boolean | null | undefined | UserDefaultArgs> = $Result.GetResult<Prisma.$UserPayload, S>

  type UserCountArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> =
    Omit<UserFindManyArgs, 'select' | 'include' | 'distinct' | 'omit'> & {
      select?: UserCountAggregateInputType | true
    }

  export interface UserDelegate<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs, GlobalOmitOptions = {}> {
    [K: symbol]: { types: Prisma.TypeMap<ExtArgs>['model']['User'], meta: { name: 'User' } }
    /**
     * Find zero or one User that matches the filter.
     * @param {UserFindUniqueArgs} args - Arguments to find a User
     * @example
     * // Get one User
     * const user = await prisma.user.findUnique({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findUnique<T extends UserFindUniqueArgs>(args: SelectSubset<T, UserFindUniqueArgs<ExtArgs>>): Prisma__UserClient<$Result.GetResult<Prisma.$UserPayload<ExtArgs>, T, "findUnique", GlobalOmitOptions> | null, null, ExtArgs, GlobalOmitOptions>

    /**
     * Find one User that matches the filter or throw an error with `error.code='P2025'`
     * if no matches were found.
     * @param {UserFindUniqueOrThrowArgs} args - Arguments to find a User
     * @example
     * // Get one User
     * const user = await prisma.user.findUniqueOrThrow({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findUniqueOrThrow<T extends UserFindUniqueOrThrowArgs>(args: SelectSubset<T, UserFindUniqueOrThrowArgs<ExtArgs>>): Prisma__UserClient<$Result.GetResult<Prisma.$UserPayload<ExtArgs>, T, "findUniqueOrThrow", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Find the first User that matches the filter.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {UserFindFirstArgs} args - Arguments to find a User
     * @example
     * // Get one User
     * const user = await prisma.user.findFirst({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findFirst<T extends UserFindFirstArgs>(args?: SelectSubset<T, UserFindFirstArgs<ExtArgs>>): Prisma__UserClient<$Result.GetResult<Prisma.$UserPayload<ExtArgs>, T, "findFirst", GlobalOmitOptions> | null, null, ExtArgs, GlobalOmitOptions>

    /**
     * Find the first User that matches the filter or
     * throw `PrismaKnownClientError` with `P2025` code if no matches were found.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {UserFindFirstOrThrowArgs} args - Arguments to find a User
     * @example
     * // Get one User
     * const user = await prisma.user.findFirstOrThrow({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findFirstOrThrow<T extends UserFindFirstOrThrowArgs>(args?: SelectSubset<T, UserFindFirstOrThrowArgs<ExtArgs>>): Prisma__UserClient<$Result.GetResult<Prisma.$UserPayload<ExtArgs>, T, "findFirstOrThrow", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Find zero or more Users that matches the filter.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {UserFindManyArgs} args - Arguments to filter and select certain fields only.
     * @example
     * // Get all Users
     * const users = await prisma.user.findMany()
     * 
     * // Get first 10 Users
     * const users = await prisma.user.findMany({ take: 10 })
     * 
     * // Only select the `id`
     * const userWithIdOnly = await prisma.user.findMany({ select: { id: true } })
     * 
     */
    findMany<T extends UserFindManyArgs>(args?: SelectSubset<T, UserFindManyArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$UserPayload<ExtArgs>, T, "findMany", GlobalOmitOptions>>

    /**
     * Create a User.
     * @param {UserCreateArgs} args - Arguments to create a User.
     * @example
     * // Create one User
     * const User = await prisma.user.create({
     *   data: {
     *     // ... data to create a User
     *   }
     * })
     * 
     */
    create<T extends UserCreateArgs>(args: SelectSubset<T, UserCreateArgs<ExtArgs>>): Prisma__UserClient<$Result.GetResult<Prisma.$UserPayload<ExtArgs>, T, "create", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Create many Users.
     * @param {UserCreateManyArgs} args - Arguments to create many Users.
     * @example
     * // Create many Users
     * const user = await prisma.user.createMany({
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     *     
     */
    createMany<T extends UserCreateManyArgs>(args?: SelectSubset<T, UserCreateManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Create many Users and returns the data saved in the database.
     * @param {UserCreateManyAndReturnArgs} args - Arguments to create many Users.
     * @example
     * // Create many Users
     * const user = await prisma.user.createManyAndReturn({
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * 
     * // Create many Users and only return the `id`
     * const userWithIdOnly = await prisma.user.createManyAndReturn({
     *   select: { id: true },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * 
     */
    createManyAndReturn<T extends UserCreateManyAndReturnArgs>(args?: SelectSubset<T, UserCreateManyAndReturnArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$UserPayload<ExtArgs>, T, "createManyAndReturn", GlobalOmitOptions>>

    /**
     * Delete a User.
     * @param {UserDeleteArgs} args - Arguments to delete one User.
     * @example
     * // Delete one User
     * const User = await prisma.user.delete({
     *   where: {
     *     // ... filter to delete one User
     *   }
     * })
     * 
     */
    delete<T extends UserDeleteArgs>(args: SelectSubset<T, UserDeleteArgs<ExtArgs>>): Prisma__UserClient<$Result.GetResult<Prisma.$UserPayload<ExtArgs>, T, "delete", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Update one User.
     * @param {UserUpdateArgs} args - Arguments to update one User.
     * @example
     * // Update one User
     * const user = await prisma.user.update({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: {
     *     // ... provide data here
     *   }
     * })
     * 
     */
    update<T extends UserUpdateArgs>(args: SelectSubset<T, UserUpdateArgs<ExtArgs>>): Prisma__UserClient<$Result.GetResult<Prisma.$UserPayload<ExtArgs>, T, "update", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Delete zero or more Users.
     * @param {UserDeleteManyArgs} args - Arguments to filter Users to delete.
     * @example
     * // Delete a few Users
     * const { count } = await prisma.user.deleteMany({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     * 
     */
    deleteMany<T extends UserDeleteManyArgs>(args?: SelectSubset<T, UserDeleteManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Update zero or more Users.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {UserUpdateManyArgs} args - Arguments to update one or more rows.
     * @example
     * // Update many Users
     * const user = await prisma.user.updateMany({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: {
     *     // ... provide data here
     *   }
     * })
     * 
     */
    updateMany<T extends UserUpdateManyArgs>(args: SelectSubset<T, UserUpdateManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Update zero or more Users and returns the data updated in the database.
     * @param {UserUpdateManyAndReturnArgs} args - Arguments to update many Users.
     * @example
     * // Update many Users
     * const user = await prisma.user.updateManyAndReturn({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * 
     * // Update zero or more Users and only return the `id`
     * const userWithIdOnly = await prisma.user.updateManyAndReturn({
     *   select: { id: true },
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * 
     */
    updateManyAndReturn<T extends UserUpdateManyAndReturnArgs>(args: SelectSubset<T, UserUpdateManyAndReturnArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$UserPayload<ExtArgs>, T, "updateManyAndReturn", GlobalOmitOptions>>

    /**
     * Create or update one User.
     * @param {UserUpsertArgs} args - Arguments to update or create a User.
     * @example
     * // Update or create a User
     * const user = await prisma.user.upsert({
     *   create: {
     *     // ... data to create a User
     *   },
     *   update: {
     *     // ... in case it already exists, update
     *   },
     *   where: {
     *     // ... the filter for the User we want to update
     *   }
     * })
     */
    upsert<T extends UserUpsertArgs>(args: SelectSubset<T, UserUpsertArgs<ExtArgs>>): Prisma__UserClient<$Result.GetResult<Prisma.$UserPayload<ExtArgs>, T, "upsert", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>


    /**
     * Count the number of Users.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {UserCountArgs} args - Arguments to filter Users to count.
     * @example
     * // Count the number of Users
     * const count = await prisma.user.count({
     *   where: {
     *     // ... the filter for the Users we want to count
     *   }
     * })
    **/
    count<T extends UserCountArgs>(
      args?: Subset<T, UserCountArgs>,
    ): Prisma.PrismaPromise<
      T extends $Utils.Record<'select', any>
        ? T['select'] extends true
          ? number
          : GetScalarType<T['select'], UserCountAggregateOutputType>
        : number
    >

    /**
     * Allows you to perform aggregations operations on a User.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {UserAggregateArgs} args - Select which aggregations you would like to apply and on what fields.
     * @example
     * // Ordered by age ascending
     * // Where email contains prisma.io
     * // Limited to the 10 users
     * const aggregations = await prisma.user.aggregate({
     *   _avg: {
     *     age: true,
     *   },
     *   where: {
     *     email: {
     *       contains: "prisma.io",
     *     },
     *   },
     *   orderBy: {
     *     age: "asc",
     *   },
     *   take: 10,
     * })
    **/
    aggregate<T extends UserAggregateArgs>(args: Subset<T, UserAggregateArgs>): Prisma.PrismaPromise<GetUserAggregateType<T>>

    /**
     * Group by User.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {UserGroupByArgs} args - Group by arguments.
     * @example
     * // Group by city, order by createdAt, get count
     * const result = await prisma.user.groupBy({
     *   by: ['city', 'createdAt'],
     *   orderBy: {
     *     createdAt: true
     *   },
     *   _count: {
     *     _all: true
     *   },
     * })
     * 
    **/
    groupBy<
      T extends UserGroupByArgs,
      HasSelectOrTake extends Or<
        Extends<'skip', Keys<T>>,
        Extends<'take', Keys<T>>
      >,
      OrderByArg extends True extends HasSelectOrTake
        ? { orderBy: UserGroupByArgs['orderBy'] }
        : { orderBy?: UserGroupByArgs['orderBy'] },
      OrderFields extends ExcludeUnderscoreKeys<Keys<MaybeTupleToUnion<T['orderBy']>>>,
      ByFields extends MaybeTupleToUnion<T['by']>,
      ByValid extends Has<ByFields, OrderFields>,
      HavingFields extends GetHavingFields<T['having']>,
      HavingValid extends Has<ByFields, HavingFields>,
      ByEmpty extends T['by'] extends never[] ? True : False,
      InputErrors extends ByEmpty extends True
      ? `Error: "by" must not be empty.`
      : HavingValid extends False
      ? {
          [P in HavingFields]: P extends ByFields
            ? never
            : P extends string
            ? `Error: Field "${P}" used in "having" needs to be provided in "by".`
            : [
                Error,
                'Field ',
                P,
                ` in "having" needs to be provided in "by"`,
              ]
        }[HavingFields]
      : 'take' extends Keys<T>
      ? 'orderBy' extends Keys<T>
        ? ByValid extends True
          ? {}
          : {
              [P in OrderFields]: P extends ByFields
                ? never
                : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
            }[OrderFields]
        : 'Error: If you provide "take", you also need to provide "orderBy"'
      : 'skip' extends Keys<T>
      ? 'orderBy' extends Keys<T>
        ? ByValid extends True
          ? {}
          : {
              [P in OrderFields]: P extends ByFields
                ? never
                : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
            }[OrderFields]
        : 'Error: If you provide "skip", you also need to provide "orderBy"'
      : ByValid extends True
      ? {}
      : {
          [P in OrderFields]: P extends ByFields
            ? never
            : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
        }[OrderFields]
    >(args: SubsetIntersection<T, UserGroupByArgs, OrderByArg> & InputErrors): {} extends InputErrors ? GetUserGroupByPayload<T> : Prisma.PrismaPromise<InputErrors>
  /**
   * Fields of the User model
   */
  readonly fields: UserFieldRefs;
  }

  /**
   * The delegate class that acts as a "Promise-like" for User.
   * Why is this prefixed with `Prisma__`?
   * Because we want to prevent naming conflicts as mentioned in
   * https://github.com/prisma/prisma-client-js/issues/707
   */
  export interface Prisma__UserClient<T, Null = never, ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs, GlobalOmitOptions = {}> extends Prisma.PrismaPromise<T> {
    readonly [Symbol.toStringTag]: "PrismaPromise"
    tasks<T extends User$tasksArgs<ExtArgs> = {}>(args?: Subset<T, User$tasksArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$TaskPayload<ExtArgs>, T, "findMany", GlobalOmitOptions> | Null>
    clients<T extends User$clientsArgs<ExtArgs> = {}>(args?: Subset<T, User$clientsArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$UserClientPayload<ExtArgs>, T, "findMany", GlobalOmitOptions> | Null>
    /**
     * Attaches callbacks for the resolution and/or rejection of the Promise.
     * @param onfulfilled The callback to execute when the Promise is resolved.
     * @param onrejected The callback to execute when the Promise is rejected.
     * @returns A Promise for the completion of which ever callback is executed.
     */
    then<TResult1 = T, TResult2 = never>(onfulfilled?: ((value: T) => TResult1 | PromiseLike<TResult1>) | undefined | null, onrejected?: ((reason: any) => TResult2 | PromiseLike<TResult2>) | undefined | null): $Utils.JsPromise<TResult1 | TResult2>
    /**
     * Attaches a callback for only the rejection of the Promise.
     * @param onrejected The callback to execute when the Promise is rejected.
     * @returns A Promise for the completion of the callback.
     */
    catch<TResult = never>(onrejected?: ((reason: any) => TResult | PromiseLike<TResult>) | undefined | null): $Utils.JsPromise<T | TResult>
    /**
     * Attaches a callback that is invoked when the Promise is settled (fulfilled or rejected). The
     * resolved value cannot be modified from the callback.
     * @param onfinally The callback to execute when the Promise is settled (fulfilled or rejected).
     * @returns A Promise for the completion of the callback.
     */
    finally(onfinally?: (() => void) | undefined | null): $Utils.JsPromise<T>
  }




  /**
   * Fields of the User model
   */
  interface UserFieldRefs {
    readonly id: FieldRef<"User", 'Int'>
    readonly email: FieldRef<"User", 'String'>
    readonly name: FieldRef<"User", 'String'>
    readonly password: FieldRef<"User", 'String'>
    readonly rol: FieldRef<"User", 'Role'>
  }
    

  // Custom InputTypes
  /**
   * User findUnique
   */
  export type UserFindUniqueArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the User
     */
    select?: UserSelect<ExtArgs> | null
    /**
     * Omit specific fields from the User
     */
    omit?: UserOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserInclude<ExtArgs> | null
    /**
     * Filter, which User to fetch.
     */
    where: UserWhereUniqueInput
  }

  /**
   * User findUniqueOrThrow
   */
  export type UserFindUniqueOrThrowArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the User
     */
    select?: UserSelect<ExtArgs> | null
    /**
     * Omit specific fields from the User
     */
    omit?: UserOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserInclude<ExtArgs> | null
    /**
     * Filter, which User to fetch.
     */
    where: UserWhereUniqueInput
  }

  /**
   * User findFirst
   */
  export type UserFindFirstArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the User
     */
    select?: UserSelect<ExtArgs> | null
    /**
     * Omit specific fields from the User
     */
    omit?: UserOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserInclude<ExtArgs> | null
    /**
     * Filter, which User to fetch.
     */
    where?: UserWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Users to fetch.
     */
    orderBy?: UserOrderByWithRelationInput | UserOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for searching for Users.
     */
    cursor?: UserWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Users from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Users.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/distinct Distinct Docs}
     * 
     * Filter by unique combinations of Users.
     */
    distinct?: UserScalarFieldEnum | UserScalarFieldEnum[]
  }

  /**
   * User findFirstOrThrow
   */
  export type UserFindFirstOrThrowArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the User
     */
    select?: UserSelect<ExtArgs> | null
    /**
     * Omit specific fields from the User
     */
    omit?: UserOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserInclude<ExtArgs> | null
    /**
     * Filter, which User to fetch.
     */
    where?: UserWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Users to fetch.
     */
    orderBy?: UserOrderByWithRelationInput | UserOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for searching for Users.
     */
    cursor?: UserWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Users from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Users.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/distinct Distinct Docs}
     * 
     * Filter by unique combinations of Users.
     */
    distinct?: UserScalarFieldEnum | UserScalarFieldEnum[]
  }

  /**
   * User findMany
   */
  export type UserFindManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the User
     */
    select?: UserSelect<ExtArgs> | null
    /**
     * Omit specific fields from the User
     */
    omit?: UserOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserInclude<ExtArgs> | null
    /**
     * Filter, which Users to fetch.
     */
    where?: UserWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Users to fetch.
     */
    orderBy?: UserOrderByWithRelationInput | UserOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for listing Users.
     */
    cursor?: UserWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Users from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Users.
     */
    skip?: number
    distinct?: UserScalarFieldEnum | UserScalarFieldEnum[]
  }

  /**
   * User create
   */
  export type UserCreateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the User
     */
    select?: UserSelect<ExtArgs> | null
    /**
     * Omit specific fields from the User
     */
    omit?: UserOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserInclude<ExtArgs> | null
    /**
     * The data needed to create a User.
     */
    data: XOR<UserCreateInput, UserUncheckedCreateInput>
  }

  /**
   * User createMany
   */
  export type UserCreateManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * The data used to create many Users.
     */
    data: UserCreateManyInput | UserCreateManyInput[]
    skipDuplicates?: boolean
  }

  /**
   * User createManyAndReturn
   */
  export type UserCreateManyAndReturnArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the User
     */
    select?: UserSelectCreateManyAndReturn<ExtArgs> | null
    /**
     * Omit specific fields from the User
     */
    omit?: UserOmit<ExtArgs> | null
    /**
     * The data used to create many Users.
     */
    data: UserCreateManyInput | UserCreateManyInput[]
    skipDuplicates?: boolean
  }

  /**
   * User update
   */
  export type UserUpdateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the User
     */
    select?: UserSelect<ExtArgs> | null
    /**
     * Omit specific fields from the User
     */
    omit?: UserOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserInclude<ExtArgs> | null
    /**
     * The data needed to update a User.
     */
    data: XOR<UserUpdateInput, UserUncheckedUpdateInput>
    /**
     * Choose, which User to update.
     */
    where: UserWhereUniqueInput
  }

  /**
   * User updateMany
   */
  export type UserUpdateManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * The data used to update Users.
     */
    data: XOR<UserUpdateManyMutationInput, UserUncheckedUpdateManyInput>
    /**
     * Filter which Users to update
     */
    where?: UserWhereInput
    /**
     * Limit how many Users to update.
     */
    limit?: number
  }

  /**
   * User updateManyAndReturn
   */
  export type UserUpdateManyAndReturnArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the User
     */
    select?: UserSelectUpdateManyAndReturn<ExtArgs> | null
    /**
     * Omit specific fields from the User
     */
    omit?: UserOmit<ExtArgs> | null
    /**
     * The data used to update Users.
     */
    data: XOR<UserUpdateManyMutationInput, UserUncheckedUpdateManyInput>
    /**
     * Filter which Users to update
     */
    where?: UserWhereInput
    /**
     * Limit how many Users to update.
     */
    limit?: number
  }

  /**
   * User upsert
   */
  export type UserUpsertArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the User
     */
    select?: UserSelect<ExtArgs> | null
    /**
     * Omit specific fields from the User
     */
    omit?: UserOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserInclude<ExtArgs> | null
    /**
     * The filter to search for the User to update in case it exists.
     */
    where: UserWhereUniqueInput
    /**
     * In case the User found by the `where` argument doesn't exist, create a new User with this data.
     */
    create: XOR<UserCreateInput, UserUncheckedCreateInput>
    /**
     * In case the User was found with the provided `where` argument, update it with this data.
     */
    update: XOR<UserUpdateInput, UserUncheckedUpdateInput>
  }

  /**
   * User delete
   */
  export type UserDeleteArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the User
     */
    select?: UserSelect<ExtArgs> | null
    /**
     * Omit specific fields from the User
     */
    omit?: UserOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserInclude<ExtArgs> | null
    /**
     * Filter which User to delete.
     */
    where: UserWhereUniqueInput
  }

  /**
   * User deleteMany
   */
  export type UserDeleteManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Filter which Users to delete
     */
    where?: UserWhereInput
    /**
     * Limit how many Users to delete.
     */
    limit?: number
  }

  /**
   * User.tasks
   */
  export type User$tasksArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Task
     */
    select?: TaskSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Task
     */
    omit?: TaskOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: TaskInclude<ExtArgs> | null
    where?: TaskWhereInput
    orderBy?: TaskOrderByWithRelationInput | TaskOrderByWithRelationInput[]
    cursor?: TaskWhereUniqueInput
    take?: number
    skip?: number
    distinct?: TaskScalarFieldEnum | TaskScalarFieldEnum[]
  }

  /**
   * User.clients
   */
  export type User$clientsArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the UserClient
     */
    select?: UserClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the UserClient
     */
    omit?: UserClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserClientInclude<ExtArgs> | null
    where?: UserClientWhereInput
    orderBy?: UserClientOrderByWithRelationInput | UserClientOrderByWithRelationInput[]
    cursor?: UserClientWhereUniqueInput
    take?: number
    skip?: number
    distinct?: UserClientScalarFieldEnum | UserClientScalarFieldEnum[]
  }

  /**
   * User without action
   */
  export type UserDefaultArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the User
     */
    select?: UserSelect<ExtArgs> | null
    /**
     * Omit specific fields from the User
     */
    omit?: UserOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserInclude<ExtArgs> | null
  }


  /**
   * Model Client
   */

  export type AggregateClient = {
    _count: ClientCountAggregateOutputType | null
    _avg: ClientAvgAggregateOutputType | null
    _sum: ClientSumAggregateOutputType | null
    _min: ClientMinAggregateOutputType | null
    _max: ClientMaxAggregateOutputType | null
  }

  export type ClientAvgAggregateOutputType = {
    id: number | null
  }

  export type ClientSumAggregateOutputType = {
    id: number | null
  }

  export type ClientMinAggregateOutputType = {
    id: number | null
    denumire: string | null
    tip: $Enums.Tip | null
    cui: string | null
    activa: boolean | null
    dataVerificarii: Date | null
    adresa: string | null
    administratie: $Enums.Administratie | null
    impozit: $Enums.Impozit | null
    platitorTVA: $Enums.DaLunarTrim | null
    tvaLaIncasare: boolean | null
    areCodTVAUE: boolean | null
    codTVAUE: string | null
    operatiuneUE: boolean | null
    dividende: boolean | null
    salariati: $Enums.DaLunarTrim | null
    casaDeMarcat: boolean | null
    dataExpSediuSocial: Date | null
    dataExpMandatAdmin: Date | null
    dataCertificatFiscal: Date | null
    dataFisaPlatitor: Date | null
    dataVectFiscal: Date | null
    createdAt: Date | null
    updatedAt: Date | null
  }

  export type ClientMaxAggregateOutputType = {
    id: number | null
    denumire: string | null
    tip: $Enums.Tip | null
    cui: string | null
    activa: boolean | null
    dataVerificarii: Date | null
    adresa: string | null
    administratie: $Enums.Administratie | null
    impozit: $Enums.Impozit | null
    platitorTVA: $Enums.DaLunarTrim | null
    tvaLaIncasare: boolean | null
    areCodTVAUE: boolean | null
    codTVAUE: string | null
    operatiuneUE: boolean | null
    dividende: boolean | null
    salariati: $Enums.DaLunarTrim | null
    casaDeMarcat: boolean | null
    dataExpSediuSocial: Date | null
    dataExpMandatAdmin: Date | null
    dataCertificatFiscal: Date | null
    dataFisaPlatitor: Date | null
    dataVectFiscal: Date | null
    createdAt: Date | null
    updatedAt: Date | null
  }

  export type ClientCountAggregateOutputType = {
    id: number
    denumire: number
    tip: number
    cui: number
    activa: number
    dataVerificarii: number
    adresa: number
    administratie: number
    impozit: number
    platitorTVA: number
    tvaLaIncasare: number
    areCodTVAUE: number
    codTVAUE: number
    operatiuneUE: number
    dividende: number
    salariati: number
    casaDeMarcat: number
    dataExpSediuSocial: number
    dataExpMandatAdmin: number
    dataCertificatFiscal: number
    dataFisaPlatitor: number
    dataVectFiscal: number
    createdAt: number
    updatedAt: number
    _all: number
  }


  export type ClientAvgAggregateInputType = {
    id?: true
  }

  export type ClientSumAggregateInputType = {
    id?: true
  }

  export type ClientMinAggregateInputType = {
    id?: true
    denumire?: true
    tip?: true
    cui?: true
    activa?: true
    dataVerificarii?: true
    adresa?: true
    administratie?: true
    impozit?: true
    platitorTVA?: true
    tvaLaIncasare?: true
    areCodTVAUE?: true
    codTVAUE?: true
    operatiuneUE?: true
    dividende?: true
    salariati?: true
    casaDeMarcat?: true
    dataExpSediuSocial?: true
    dataExpMandatAdmin?: true
    dataCertificatFiscal?: true
    dataFisaPlatitor?: true
    dataVectFiscal?: true
    createdAt?: true
    updatedAt?: true
  }

  export type ClientMaxAggregateInputType = {
    id?: true
    denumire?: true
    tip?: true
    cui?: true
    activa?: true
    dataVerificarii?: true
    adresa?: true
    administratie?: true
    impozit?: true
    platitorTVA?: true
    tvaLaIncasare?: true
    areCodTVAUE?: true
    codTVAUE?: true
    operatiuneUE?: true
    dividende?: true
    salariati?: true
    casaDeMarcat?: true
    dataExpSediuSocial?: true
    dataExpMandatAdmin?: true
    dataCertificatFiscal?: true
    dataFisaPlatitor?: true
    dataVectFiscal?: true
    createdAt?: true
    updatedAt?: true
  }

  export type ClientCountAggregateInputType = {
    id?: true
    denumire?: true
    tip?: true
    cui?: true
    activa?: true
    dataVerificarii?: true
    adresa?: true
    administratie?: true
    impozit?: true
    platitorTVA?: true
    tvaLaIncasare?: true
    areCodTVAUE?: true
    codTVAUE?: true
    operatiuneUE?: true
    dividende?: true
    salariati?: true
    casaDeMarcat?: true
    dataExpSediuSocial?: true
    dataExpMandatAdmin?: true
    dataCertificatFiscal?: true
    dataFisaPlatitor?: true
    dataVectFiscal?: true
    createdAt?: true
    updatedAt?: true
    _all?: true
  }

  export type ClientAggregateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Filter which Client to aggregate.
     */
    where?: ClientWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Clients to fetch.
     */
    orderBy?: ClientOrderByWithRelationInput | ClientOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the start position
     */
    cursor?: ClientWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Clients from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Clients.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Count returned Clients
    **/
    _count?: true | ClientCountAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to average
    **/
    _avg?: ClientAvgAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to sum
    **/
    _sum?: ClientSumAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to find the minimum value
    **/
    _min?: ClientMinAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to find the maximum value
    **/
    _max?: ClientMaxAggregateInputType
  }

  export type GetClientAggregateType<T extends ClientAggregateArgs> = {
        [P in keyof T & keyof AggregateClient]: P extends '_count' | 'count'
      ? T[P] extends true
        ? number
        : GetScalarType<T[P], AggregateClient[P]>
      : GetScalarType<T[P], AggregateClient[P]>
  }




  export type ClientGroupByArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    where?: ClientWhereInput
    orderBy?: ClientOrderByWithAggregationInput | ClientOrderByWithAggregationInput[]
    by: ClientScalarFieldEnum[] | ClientScalarFieldEnum
    having?: ClientScalarWhereWithAggregatesInput
    take?: number
    skip?: number
    _count?: ClientCountAggregateInputType | true
    _avg?: ClientAvgAggregateInputType
    _sum?: ClientSumAggregateInputType
    _min?: ClientMinAggregateInputType
    _max?: ClientMaxAggregateInputType
  }

  export type ClientGroupByOutputType = {
    id: number
    denumire: string
    tip: $Enums.Tip
    cui: string
    activa: boolean
    dataVerificarii: Date | null
    adresa: string | null
    administratie: $Enums.Administratie
    impozit: $Enums.Impozit | null
    platitorTVA: $Enums.DaLunarTrim
    tvaLaIncasare: boolean | null
    areCodTVAUE: boolean | null
    codTVAUE: string | null
    operatiuneUE: boolean | null
    dividende: boolean | null
    salariati: $Enums.DaLunarTrim | null
    casaDeMarcat: boolean | null
    dataExpSediuSocial: Date | null
    dataExpMandatAdmin: Date | null
    dataCertificatFiscal: Date | null
    dataFisaPlatitor: Date | null
    dataVectFiscal: Date | null
    createdAt: Date
    updatedAt: Date
    _count: ClientCountAggregateOutputType | null
    _avg: ClientAvgAggregateOutputType | null
    _sum: ClientSumAggregateOutputType | null
    _min: ClientMinAggregateOutputType | null
    _max: ClientMaxAggregateOutputType | null
  }

  type GetClientGroupByPayload<T extends ClientGroupByArgs> = Prisma.PrismaPromise<
    Array<
      PickEnumerable<ClientGroupByOutputType, T['by']> &
        {
          [P in ((keyof T) & (keyof ClientGroupByOutputType))]: P extends '_count'
            ? T[P] extends boolean
              ? number
              : GetScalarType<T[P], ClientGroupByOutputType[P]>
            : GetScalarType<T[P], ClientGroupByOutputType[P]>
        }
      >
    >


  export type ClientSelect<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    denumire?: boolean
    tip?: boolean
    cui?: boolean
    activa?: boolean
    dataVerificarii?: boolean
    adresa?: boolean
    administratie?: boolean
    impozit?: boolean
    platitorTVA?: boolean
    tvaLaIncasare?: boolean
    areCodTVAUE?: boolean
    codTVAUE?: boolean
    operatiuneUE?: boolean
    dividende?: boolean
    salariati?: boolean
    casaDeMarcat?: boolean
    dataExpSediuSocial?: boolean
    dataExpMandatAdmin?: boolean
    dataCertificatFiscal?: boolean
    dataFisaPlatitor?: boolean
    dataVectFiscal?: boolean
    createdAt?: boolean
    updatedAt?: boolean
    detalii?: boolean | Client$detaliiArgs<ExtArgs>
    puncteDeLucru?: boolean | Client$puncteDeLucruArgs<ExtArgs>
    istorice?: boolean | Client$istoriceArgs<ExtArgs>
    tasks?: boolean | Client$tasksArgs<ExtArgs>
    users?: boolean | Client$usersArgs<ExtArgs>
    _count?: boolean | ClientCountOutputTypeDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["client"]>

  export type ClientSelectCreateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    denumire?: boolean
    tip?: boolean
    cui?: boolean
    activa?: boolean
    dataVerificarii?: boolean
    adresa?: boolean
    administratie?: boolean
    impozit?: boolean
    platitorTVA?: boolean
    tvaLaIncasare?: boolean
    areCodTVAUE?: boolean
    codTVAUE?: boolean
    operatiuneUE?: boolean
    dividende?: boolean
    salariati?: boolean
    casaDeMarcat?: boolean
    dataExpSediuSocial?: boolean
    dataExpMandatAdmin?: boolean
    dataCertificatFiscal?: boolean
    dataFisaPlatitor?: boolean
    dataVectFiscal?: boolean
    createdAt?: boolean
    updatedAt?: boolean
  }, ExtArgs["result"]["client"]>

  export type ClientSelectUpdateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    denumire?: boolean
    tip?: boolean
    cui?: boolean
    activa?: boolean
    dataVerificarii?: boolean
    adresa?: boolean
    administratie?: boolean
    impozit?: boolean
    platitorTVA?: boolean
    tvaLaIncasare?: boolean
    areCodTVAUE?: boolean
    codTVAUE?: boolean
    operatiuneUE?: boolean
    dividende?: boolean
    salariati?: boolean
    casaDeMarcat?: boolean
    dataExpSediuSocial?: boolean
    dataExpMandatAdmin?: boolean
    dataCertificatFiscal?: boolean
    dataFisaPlatitor?: boolean
    dataVectFiscal?: boolean
    createdAt?: boolean
    updatedAt?: boolean
  }, ExtArgs["result"]["client"]>

  export type ClientSelectScalar = {
    id?: boolean
    denumire?: boolean
    tip?: boolean
    cui?: boolean
    activa?: boolean
    dataVerificarii?: boolean
    adresa?: boolean
    administratie?: boolean
    impozit?: boolean
    platitorTVA?: boolean
    tvaLaIncasare?: boolean
    areCodTVAUE?: boolean
    codTVAUE?: boolean
    operatiuneUE?: boolean
    dividende?: boolean
    salariati?: boolean
    casaDeMarcat?: boolean
    dataExpSediuSocial?: boolean
    dataExpMandatAdmin?: boolean
    dataCertificatFiscal?: boolean
    dataFisaPlatitor?: boolean
    dataVectFiscal?: boolean
    createdAt?: boolean
    updatedAt?: boolean
  }

  export type ClientOmit<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetOmit<"id" | "denumire" | "tip" | "cui" | "activa" | "dataVerificarii" | "adresa" | "administratie" | "impozit" | "platitorTVA" | "tvaLaIncasare" | "areCodTVAUE" | "codTVAUE" | "operatiuneUE" | "dividende" | "salariati" | "casaDeMarcat" | "dataExpSediuSocial" | "dataExpMandatAdmin" | "dataCertificatFiscal" | "dataFisaPlatitor" | "dataVectFiscal" | "createdAt" | "updatedAt", ExtArgs["result"]["client"]>
  export type ClientInclude<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    detalii?: boolean | Client$detaliiArgs<ExtArgs>
    puncteDeLucru?: boolean | Client$puncteDeLucruArgs<ExtArgs>
    istorice?: boolean | Client$istoriceArgs<ExtArgs>
    tasks?: boolean | Client$tasksArgs<ExtArgs>
    users?: boolean | Client$usersArgs<ExtArgs>
    _count?: boolean | ClientCountOutputTypeDefaultArgs<ExtArgs>
  }
  export type ClientIncludeCreateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {}
  export type ClientIncludeUpdateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {}

  export type $ClientPayload<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    name: "Client"
    objects: {
      detalii: Prisma.$DetaliiPayload<ExtArgs> | null
      puncteDeLucru: Prisma.$PunctDeLucruPayload<ExtArgs>[]
      istorice: Prisma.$IstoricPayload<ExtArgs>[]
      tasks: Prisma.$TaskPayload<ExtArgs>[]
      users: Prisma.$UserClientPayload<ExtArgs>[]
    }
    scalars: $Extensions.GetPayloadResult<{
      id: number
      denumire: string
      tip: $Enums.Tip
      cui: string
      activa: boolean
      dataVerificarii: Date | null
      adresa: string | null
      administratie: $Enums.Administratie
      impozit: $Enums.Impozit | null
      platitorTVA: $Enums.DaLunarTrim
      tvaLaIncasare: boolean | null
      areCodTVAUE: boolean | null
      codTVAUE: string | null
      operatiuneUE: boolean | null
      dividende: boolean | null
      salariati: $Enums.DaLunarTrim | null
      casaDeMarcat: boolean | null
      dataExpSediuSocial: Date | null
      dataExpMandatAdmin: Date | null
      dataCertificatFiscal: Date | null
      dataFisaPlatitor: Date | null
      dataVectFiscal: Date | null
      createdAt: Date
      updatedAt: Date
    }, ExtArgs["result"]["client"]>
    composites: {}
  }

  type ClientGetPayload<S extends boolean | null | undefined | ClientDefaultArgs> = $Result.GetResult<Prisma.$ClientPayload, S>

  type ClientCountArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> =
    Omit<ClientFindManyArgs, 'select' | 'include' | 'distinct' | 'omit'> & {
      select?: ClientCountAggregateInputType | true
    }

  export interface ClientDelegate<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs, GlobalOmitOptions = {}> {
    [K: symbol]: { types: Prisma.TypeMap<ExtArgs>['model']['Client'], meta: { name: 'Client' } }
    /**
     * Find zero or one Client that matches the filter.
     * @param {ClientFindUniqueArgs} args - Arguments to find a Client
     * @example
     * // Get one Client
     * const client = await prisma.client.findUnique({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findUnique<T extends ClientFindUniqueArgs>(args: SelectSubset<T, ClientFindUniqueArgs<ExtArgs>>): Prisma__ClientClient<$Result.GetResult<Prisma.$ClientPayload<ExtArgs>, T, "findUnique", GlobalOmitOptions> | null, null, ExtArgs, GlobalOmitOptions>

    /**
     * Find one Client that matches the filter or throw an error with `error.code='P2025'`
     * if no matches were found.
     * @param {ClientFindUniqueOrThrowArgs} args - Arguments to find a Client
     * @example
     * // Get one Client
     * const client = await prisma.client.findUniqueOrThrow({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findUniqueOrThrow<T extends ClientFindUniqueOrThrowArgs>(args: SelectSubset<T, ClientFindUniqueOrThrowArgs<ExtArgs>>): Prisma__ClientClient<$Result.GetResult<Prisma.$ClientPayload<ExtArgs>, T, "findUniqueOrThrow", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Find the first Client that matches the filter.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {ClientFindFirstArgs} args - Arguments to find a Client
     * @example
     * // Get one Client
     * const client = await prisma.client.findFirst({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findFirst<T extends ClientFindFirstArgs>(args?: SelectSubset<T, ClientFindFirstArgs<ExtArgs>>): Prisma__ClientClient<$Result.GetResult<Prisma.$ClientPayload<ExtArgs>, T, "findFirst", GlobalOmitOptions> | null, null, ExtArgs, GlobalOmitOptions>

    /**
     * Find the first Client that matches the filter or
     * throw `PrismaKnownClientError` with `P2025` code if no matches were found.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {ClientFindFirstOrThrowArgs} args - Arguments to find a Client
     * @example
     * // Get one Client
     * const client = await prisma.client.findFirstOrThrow({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findFirstOrThrow<T extends ClientFindFirstOrThrowArgs>(args?: SelectSubset<T, ClientFindFirstOrThrowArgs<ExtArgs>>): Prisma__ClientClient<$Result.GetResult<Prisma.$ClientPayload<ExtArgs>, T, "findFirstOrThrow", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Find zero or more Clients that matches the filter.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {ClientFindManyArgs} args - Arguments to filter and select certain fields only.
     * @example
     * // Get all Clients
     * const clients = await prisma.client.findMany()
     * 
     * // Get first 10 Clients
     * const clients = await prisma.client.findMany({ take: 10 })
     * 
     * // Only select the `id`
     * const clientWithIdOnly = await prisma.client.findMany({ select: { id: true } })
     * 
     */
    findMany<T extends ClientFindManyArgs>(args?: SelectSubset<T, ClientFindManyArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$ClientPayload<ExtArgs>, T, "findMany", GlobalOmitOptions>>

    /**
     * Create a Client.
     * @param {ClientCreateArgs} args - Arguments to create a Client.
     * @example
     * // Create one Client
     * const Client = await prisma.client.create({
     *   data: {
     *     // ... data to create a Client
     *   }
     * })
     * 
     */
    create<T extends ClientCreateArgs>(args: SelectSubset<T, ClientCreateArgs<ExtArgs>>): Prisma__ClientClient<$Result.GetResult<Prisma.$ClientPayload<ExtArgs>, T, "create", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Create many Clients.
     * @param {ClientCreateManyArgs} args - Arguments to create many Clients.
     * @example
     * // Create many Clients
     * const client = await prisma.client.createMany({
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     *     
     */
    createMany<T extends ClientCreateManyArgs>(args?: SelectSubset<T, ClientCreateManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Create many Clients and returns the data saved in the database.
     * @param {ClientCreateManyAndReturnArgs} args - Arguments to create many Clients.
     * @example
     * // Create many Clients
     * const client = await prisma.client.createManyAndReturn({
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * 
     * // Create many Clients and only return the `id`
     * const clientWithIdOnly = await prisma.client.createManyAndReturn({
     *   select: { id: true },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * 
     */
    createManyAndReturn<T extends ClientCreateManyAndReturnArgs>(args?: SelectSubset<T, ClientCreateManyAndReturnArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$ClientPayload<ExtArgs>, T, "createManyAndReturn", GlobalOmitOptions>>

    /**
     * Delete a Client.
     * @param {ClientDeleteArgs} args - Arguments to delete one Client.
     * @example
     * // Delete one Client
     * const Client = await prisma.client.delete({
     *   where: {
     *     // ... filter to delete one Client
     *   }
     * })
     * 
     */
    delete<T extends ClientDeleteArgs>(args: SelectSubset<T, ClientDeleteArgs<ExtArgs>>): Prisma__ClientClient<$Result.GetResult<Prisma.$ClientPayload<ExtArgs>, T, "delete", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Update one Client.
     * @param {ClientUpdateArgs} args - Arguments to update one Client.
     * @example
     * // Update one Client
     * const client = await prisma.client.update({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: {
     *     // ... provide data here
     *   }
     * })
     * 
     */
    update<T extends ClientUpdateArgs>(args: SelectSubset<T, ClientUpdateArgs<ExtArgs>>): Prisma__ClientClient<$Result.GetResult<Prisma.$ClientPayload<ExtArgs>, T, "update", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Delete zero or more Clients.
     * @param {ClientDeleteManyArgs} args - Arguments to filter Clients to delete.
     * @example
     * // Delete a few Clients
     * const { count } = await prisma.client.deleteMany({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     * 
     */
    deleteMany<T extends ClientDeleteManyArgs>(args?: SelectSubset<T, ClientDeleteManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Update zero or more Clients.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {ClientUpdateManyArgs} args - Arguments to update one or more rows.
     * @example
     * // Update many Clients
     * const client = await prisma.client.updateMany({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: {
     *     // ... provide data here
     *   }
     * })
     * 
     */
    updateMany<T extends ClientUpdateManyArgs>(args: SelectSubset<T, ClientUpdateManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Update zero or more Clients and returns the data updated in the database.
     * @param {ClientUpdateManyAndReturnArgs} args - Arguments to update many Clients.
     * @example
     * // Update many Clients
     * const client = await prisma.client.updateManyAndReturn({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * 
     * // Update zero or more Clients and only return the `id`
     * const clientWithIdOnly = await prisma.client.updateManyAndReturn({
     *   select: { id: true },
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * 
     */
    updateManyAndReturn<T extends ClientUpdateManyAndReturnArgs>(args: SelectSubset<T, ClientUpdateManyAndReturnArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$ClientPayload<ExtArgs>, T, "updateManyAndReturn", GlobalOmitOptions>>

    /**
     * Create or update one Client.
     * @param {ClientUpsertArgs} args - Arguments to update or create a Client.
     * @example
     * // Update or create a Client
     * const client = await prisma.client.upsert({
     *   create: {
     *     // ... data to create a Client
     *   },
     *   update: {
     *     // ... in case it already exists, update
     *   },
     *   where: {
     *     // ... the filter for the Client we want to update
     *   }
     * })
     */
    upsert<T extends ClientUpsertArgs>(args: SelectSubset<T, ClientUpsertArgs<ExtArgs>>): Prisma__ClientClient<$Result.GetResult<Prisma.$ClientPayload<ExtArgs>, T, "upsert", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>


    /**
     * Count the number of Clients.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {ClientCountArgs} args - Arguments to filter Clients to count.
     * @example
     * // Count the number of Clients
     * const count = await prisma.client.count({
     *   where: {
     *     // ... the filter for the Clients we want to count
     *   }
     * })
    **/
    count<T extends ClientCountArgs>(
      args?: Subset<T, ClientCountArgs>,
    ): Prisma.PrismaPromise<
      T extends $Utils.Record<'select', any>
        ? T['select'] extends true
          ? number
          : GetScalarType<T['select'], ClientCountAggregateOutputType>
        : number
    >

    /**
     * Allows you to perform aggregations operations on a Client.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {ClientAggregateArgs} args - Select which aggregations you would like to apply and on what fields.
     * @example
     * // Ordered by age ascending
     * // Where email contains prisma.io
     * // Limited to the 10 users
     * const aggregations = await prisma.user.aggregate({
     *   _avg: {
     *     age: true,
     *   },
     *   where: {
     *     email: {
     *       contains: "prisma.io",
     *     },
     *   },
     *   orderBy: {
     *     age: "asc",
     *   },
     *   take: 10,
     * })
    **/
    aggregate<T extends ClientAggregateArgs>(args: Subset<T, ClientAggregateArgs>): Prisma.PrismaPromise<GetClientAggregateType<T>>

    /**
     * Group by Client.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {ClientGroupByArgs} args - Group by arguments.
     * @example
     * // Group by city, order by createdAt, get count
     * const result = await prisma.user.groupBy({
     *   by: ['city', 'createdAt'],
     *   orderBy: {
     *     createdAt: true
     *   },
     *   _count: {
     *     _all: true
     *   },
     * })
     * 
    **/
    groupBy<
      T extends ClientGroupByArgs,
      HasSelectOrTake extends Or<
        Extends<'skip', Keys<T>>,
        Extends<'take', Keys<T>>
      >,
      OrderByArg extends True extends HasSelectOrTake
        ? { orderBy: ClientGroupByArgs['orderBy'] }
        : { orderBy?: ClientGroupByArgs['orderBy'] },
      OrderFields extends ExcludeUnderscoreKeys<Keys<MaybeTupleToUnion<T['orderBy']>>>,
      ByFields extends MaybeTupleToUnion<T['by']>,
      ByValid extends Has<ByFields, OrderFields>,
      HavingFields extends GetHavingFields<T['having']>,
      HavingValid extends Has<ByFields, HavingFields>,
      ByEmpty extends T['by'] extends never[] ? True : False,
      InputErrors extends ByEmpty extends True
      ? `Error: "by" must not be empty.`
      : HavingValid extends False
      ? {
          [P in HavingFields]: P extends ByFields
            ? never
            : P extends string
            ? `Error: Field "${P}" used in "having" needs to be provided in "by".`
            : [
                Error,
                'Field ',
                P,
                ` in "having" needs to be provided in "by"`,
              ]
        }[HavingFields]
      : 'take' extends Keys<T>
      ? 'orderBy' extends Keys<T>
        ? ByValid extends True
          ? {}
          : {
              [P in OrderFields]: P extends ByFields
                ? never
                : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
            }[OrderFields]
        : 'Error: If you provide "take", you also need to provide "orderBy"'
      : 'skip' extends Keys<T>
      ? 'orderBy' extends Keys<T>
        ? ByValid extends True
          ? {}
          : {
              [P in OrderFields]: P extends ByFields
                ? never
                : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
            }[OrderFields]
        : 'Error: If you provide "skip", you also need to provide "orderBy"'
      : ByValid extends True
      ? {}
      : {
          [P in OrderFields]: P extends ByFields
            ? never
            : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
        }[OrderFields]
    >(args: SubsetIntersection<T, ClientGroupByArgs, OrderByArg> & InputErrors): {} extends InputErrors ? GetClientGroupByPayload<T> : Prisma.PrismaPromise<InputErrors>
  /**
   * Fields of the Client model
   */
  readonly fields: ClientFieldRefs;
  }

  /**
   * The delegate class that acts as a "Promise-like" for Client.
   * Why is this prefixed with `Prisma__`?
   * Because we want to prevent naming conflicts as mentioned in
   * https://github.com/prisma/prisma-client-js/issues/707
   */
  export interface Prisma__ClientClient<T, Null = never, ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs, GlobalOmitOptions = {}> extends Prisma.PrismaPromise<T> {
    readonly [Symbol.toStringTag]: "PrismaPromise"
    detalii<T extends Client$detaliiArgs<ExtArgs> = {}>(args?: Subset<T, Client$detaliiArgs<ExtArgs>>): Prisma__DetaliiClient<$Result.GetResult<Prisma.$DetaliiPayload<ExtArgs>, T, "findUniqueOrThrow", GlobalOmitOptions> | null, null, ExtArgs, GlobalOmitOptions>
    puncteDeLucru<T extends Client$puncteDeLucruArgs<ExtArgs> = {}>(args?: Subset<T, Client$puncteDeLucruArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$PunctDeLucruPayload<ExtArgs>, T, "findMany", GlobalOmitOptions> | Null>
    istorice<T extends Client$istoriceArgs<ExtArgs> = {}>(args?: Subset<T, Client$istoriceArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$IstoricPayload<ExtArgs>, T, "findMany", GlobalOmitOptions> | Null>
    tasks<T extends Client$tasksArgs<ExtArgs> = {}>(args?: Subset<T, Client$tasksArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$TaskPayload<ExtArgs>, T, "findMany", GlobalOmitOptions> | Null>
    users<T extends Client$usersArgs<ExtArgs> = {}>(args?: Subset<T, Client$usersArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$UserClientPayload<ExtArgs>, T, "findMany", GlobalOmitOptions> | Null>
    /**
     * Attaches callbacks for the resolution and/or rejection of the Promise.
     * @param onfulfilled The callback to execute when the Promise is resolved.
     * @param onrejected The callback to execute when the Promise is rejected.
     * @returns A Promise for the completion of which ever callback is executed.
     */
    then<TResult1 = T, TResult2 = never>(onfulfilled?: ((value: T) => TResult1 | PromiseLike<TResult1>) | undefined | null, onrejected?: ((reason: any) => TResult2 | PromiseLike<TResult2>) | undefined | null): $Utils.JsPromise<TResult1 | TResult2>
    /**
     * Attaches a callback for only the rejection of the Promise.
     * @param onrejected The callback to execute when the Promise is rejected.
     * @returns A Promise for the completion of the callback.
     */
    catch<TResult = never>(onrejected?: ((reason: any) => TResult | PromiseLike<TResult>) | undefined | null): $Utils.JsPromise<T | TResult>
    /**
     * Attaches a callback that is invoked when the Promise is settled (fulfilled or rejected). The
     * resolved value cannot be modified from the callback.
     * @param onfinally The callback to execute when the Promise is settled (fulfilled or rejected).
     * @returns A Promise for the completion of the callback.
     */
    finally(onfinally?: (() => void) | undefined | null): $Utils.JsPromise<T>
  }




  /**
   * Fields of the Client model
   */
  interface ClientFieldRefs {
    readonly id: FieldRef<"Client", 'Int'>
    readonly denumire: FieldRef<"Client", 'String'>
    readonly tip: FieldRef<"Client", 'Tip'>
    readonly cui: FieldRef<"Client", 'String'>
    readonly activa: FieldRef<"Client", 'Boolean'>
    readonly dataVerificarii: FieldRef<"Client", 'DateTime'>
    readonly adresa: FieldRef<"Client", 'String'>
    readonly administratie: FieldRef<"Client", 'Administratie'>
    readonly impozit: FieldRef<"Client", 'Impozit'>
    readonly platitorTVA: FieldRef<"Client", 'DaLunarTrim'>
    readonly tvaLaIncasare: FieldRef<"Client", 'Boolean'>
    readonly areCodTVAUE: FieldRef<"Client", 'Boolean'>
    readonly codTVAUE: FieldRef<"Client", 'String'>
    readonly operatiuneUE: FieldRef<"Client", 'Boolean'>
    readonly dividende: FieldRef<"Client", 'Boolean'>
    readonly salariati: FieldRef<"Client", 'DaLunarTrim'>
    readonly casaDeMarcat: FieldRef<"Client", 'Boolean'>
    readonly dataExpSediuSocial: FieldRef<"Client", 'DateTime'>
    readonly dataExpMandatAdmin: FieldRef<"Client", 'DateTime'>
    readonly dataCertificatFiscal: FieldRef<"Client", 'DateTime'>
    readonly dataFisaPlatitor: FieldRef<"Client", 'DateTime'>
    readonly dataVectFiscal: FieldRef<"Client", 'DateTime'>
    readonly createdAt: FieldRef<"Client", 'DateTime'>
    readonly updatedAt: FieldRef<"Client", 'DateTime'>
  }
    

  // Custom InputTypes
  /**
   * Client findUnique
   */
  export type ClientFindUniqueArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Client
     */
    select?: ClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Client
     */
    omit?: ClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: ClientInclude<ExtArgs> | null
    /**
     * Filter, which Client to fetch.
     */
    where: ClientWhereUniqueInput
  }

  /**
   * Client findUniqueOrThrow
   */
  export type ClientFindUniqueOrThrowArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Client
     */
    select?: ClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Client
     */
    omit?: ClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: ClientInclude<ExtArgs> | null
    /**
     * Filter, which Client to fetch.
     */
    where: ClientWhereUniqueInput
  }

  /**
   * Client findFirst
   */
  export type ClientFindFirstArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Client
     */
    select?: ClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Client
     */
    omit?: ClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: ClientInclude<ExtArgs> | null
    /**
     * Filter, which Client to fetch.
     */
    where?: ClientWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Clients to fetch.
     */
    orderBy?: ClientOrderByWithRelationInput | ClientOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for searching for Clients.
     */
    cursor?: ClientWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Clients from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Clients.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/distinct Distinct Docs}
     * 
     * Filter by unique combinations of Clients.
     */
    distinct?: ClientScalarFieldEnum | ClientScalarFieldEnum[]
  }

  /**
   * Client findFirstOrThrow
   */
  export type ClientFindFirstOrThrowArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Client
     */
    select?: ClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Client
     */
    omit?: ClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: ClientInclude<ExtArgs> | null
    /**
     * Filter, which Client to fetch.
     */
    where?: ClientWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Clients to fetch.
     */
    orderBy?: ClientOrderByWithRelationInput | ClientOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for searching for Clients.
     */
    cursor?: ClientWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Clients from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Clients.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/distinct Distinct Docs}
     * 
     * Filter by unique combinations of Clients.
     */
    distinct?: ClientScalarFieldEnum | ClientScalarFieldEnum[]
  }

  /**
   * Client findMany
   */
  export type ClientFindManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Client
     */
    select?: ClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Client
     */
    omit?: ClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: ClientInclude<ExtArgs> | null
    /**
     * Filter, which Clients to fetch.
     */
    where?: ClientWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Clients to fetch.
     */
    orderBy?: ClientOrderByWithRelationInput | ClientOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for listing Clients.
     */
    cursor?: ClientWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Clients from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Clients.
     */
    skip?: number
    distinct?: ClientScalarFieldEnum | ClientScalarFieldEnum[]
  }

  /**
   * Client create
   */
  export type ClientCreateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Client
     */
    select?: ClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Client
     */
    omit?: ClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: ClientInclude<ExtArgs> | null
    /**
     * The data needed to create a Client.
     */
    data: XOR<ClientCreateInput, ClientUncheckedCreateInput>
  }

  /**
   * Client createMany
   */
  export type ClientCreateManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * The data used to create many Clients.
     */
    data: ClientCreateManyInput | ClientCreateManyInput[]
    skipDuplicates?: boolean
  }

  /**
   * Client createManyAndReturn
   */
  export type ClientCreateManyAndReturnArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Client
     */
    select?: ClientSelectCreateManyAndReturn<ExtArgs> | null
    /**
     * Omit specific fields from the Client
     */
    omit?: ClientOmit<ExtArgs> | null
    /**
     * The data used to create many Clients.
     */
    data: ClientCreateManyInput | ClientCreateManyInput[]
    skipDuplicates?: boolean
  }

  /**
   * Client update
   */
  export type ClientUpdateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Client
     */
    select?: ClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Client
     */
    omit?: ClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: ClientInclude<ExtArgs> | null
    /**
     * The data needed to update a Client.
     */
    data: XOR<ClientUpdateInput, ClientUncheckedUpdateInput>
    /**
     * Choose, which Client to update.
     */
    where: ClientWhereUniqueInput
  }

  /**
   * Client updateMany
   */
  export type ClientUpdateManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * The data used to update Clients.
     */
    data: XOR<ClientUpdateManyMutationInput, ClientUncheckedUpdateManyInput>
    /**
     * Filter which Clients to update
     */
    where?: ClientWhereInput
    /**
     * Limit how many Clients to update.
     */
    limit?: number
  }

  /**
   * Client updateManyAndReturn
   */
  export type ClientUpdateManyAndReturnArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Client
     */
    select?: ClientSelectUpdateManyAndReturn<ExtArgs> | null
    /**
     * Omit specific fields from the Client
     */
    omit?: ClientOmit<ExtArgs> | null
    /**
     * The data used to update Clients.
     */
    data: XOR<ClientUpdateManyMutationInput, ClientUncheckedUpdateManyInput>
    /**
     * Filter which Clients to update
     */
    where?: ClientWhereInput
    /**
     * Limit how many Clients to update.
     */
    limit?: number
  }

  /**
   * Client upsert
   */
  export type ClientUpsertArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Client
     */
    select?: ClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Client
     */
    omit?: ClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: ClientInclude<ExtArgs> | null
    /**
     * The filter to search for the Client to update in case it exists.
     */
    where: ClientWhereUniqueInput
    /**
     * In case the Client found by the `where` argument doesn't exist, create a new Client with this data.
     */
    create: XOR<ClientCreateInput, ClientUncheckedCreateInput>
    /**
     * In case the Client was found with the provided `where` argument, update it with this data.
     */
    update: XOR<ClientUpdateInput, ClientUncheckedUpdateInput>
  }

  /**
   * Client delete
   */
  export type ClientDeleteArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Client
     */
    select?: ClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Client
     */
    omit?: ClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: ClientInclude<ExtArgs> | null
    /**
     * Filter which Client to delete.
     */
    where: ClientWhereUniqueInput
  }

  /**
   * Client deleteMany
   */
  export type ClientDeleteManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Filter which Clients to delete
     */
    where?: ClientWhereInput
    /**
     * Limit how many Clients to delete.
     */
    limit?: number
  }

  /**
   * Client.detalii
   */
  export type Client$detaliiArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Detalii
     */
    select?: DetaliiSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Detalii
     */
    omit?: DetaliiOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: DetaliiInclude<ExtArgs> | null
    where?: DetaliiWhereInput
  }

  /**
   * Client.puncteDeLucru
   */
  export type Client$puncteDeLucruArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the PunctDeLucru
     */
    select?: PunctDeLucruSelect<ExtArgs> | null
    /**
     * Omit specific fields from the PunctDeLucru
     */
    omit?: PunctDeLucruOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: PunctDeLucruInclude<ExtArgs> | null
    where?: PunctDeLucruWhereInput
    orderBy?: PunctDeLucruOrderByWithRelationInput | PunctDeLucruOrderByWithRelationInput[]
    cursor?: PunctDeLucruWhereUniqueInput
    take?: number
    skip?: number
    distinct?: PunctDeLucruScalarFieldEnum | PunctDeLucruScalarFieldEnum[]
  }

  /**
   * Client.istorice
   */
  export type Client$istoriceArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Istoric
     */
    select?: IstoricSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Istoric
     */
    omit?: IstoricOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: IstoricInclude<ExtArgs> | null
    where?: IstoricWhereInput
    orderBy?: IstoricOrderByWithRelationInput | IstoricOrderByWithRelationInput[]
    cursor?: IstoricWhereUniqueInput
    take?: number
    skip?: number
    distinct?: IstoricScalarFieldEnum | IstoricScalarFieldEnum[]
  }

  /**
   * Client.tasks
   */
  export type Client$tasksArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Task
     */
    select?: TaskSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Task
     */
    omit?: TaskOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: TaskInclude<ExtArgs> | null
    where?: TaskWhereInput
    orderBy?: TaskOrderByWithRelationInput | TaskOrderByWithRelationInput[]
    cursor?: TaskWhereUniqueInput
    take?: number
    skip?: number
    distinct?: TaskScalarFieldEnum | TaskScalarFieldEnum[]
  }

  /**
   * Client.users
   */
  export type Client$usersArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the UserClient
     */
    select?: UserClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the UserClient
     */
    omit?: UserClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserClientInclude<ExtArgs> | null
    where?: UserClientWhereInput
    orderBy?: UserClientOrderByWithRelationInput | UserClientOrderByWithRelationInput[]
    cursor?: UserClientWhereUniqueInput
    take?: number
    skip?: number
    distinct?: UserClientScalarFieldEnum | UserClientScalarFieldEnum[]
  }

  /**
   * Client without action
   */
  export type ClientDefaultArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Client
     */
    select?: ClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Client
     */
    omit?: ClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: ClientInclude<ExtArgs> | null
  }


  /**
   * Model Detalii
   */

  export type AggregateDetalii = {
    _count: DetaliiCountAggregateOutputType | null
    _avg: DetaliiAvgAggregateOutputType | null
    _sum: DetaliiSumAggregateOutputType | null
    _min: DetaliiMinAggregateOutputType | null
    _max: DetaliiMaxAggregateOutputType | null
  }

  export type DetaliiAvgAggregateOutputType = {
    id: number | null
    clientId: number | null
  }

  export type DetaliiSumAggregateOutputType = {
    id: number | null
    clientId: number | null
  }

  export type DetaliiMinAggregateOutputType = {
    id: number | null
    registruUC: boolean | null
    registruEvFiscala: $Enums.DaNuNuECazul | null
    ofSpalareBani: boolean | null
    regulamentOrdineInterioara: boolean | null
    manualPoliticiContabile: boolean | null
    adresaRevisal: boolean | null
    parolaITM: string | null
    depunereDeclaratiiOnline: boolean | null
    accesDosarFiscal: $Enums.DaNuNuECazul | null
    clientId: number | null
  }

  export type DetaliiMaxAggregateOutputType = {
    id: number | null
    registruUC: boolean | null
    registruEvFiscala: $Enums.DaNuNuECazul | null
    ofSpalareBani: boolean | null
    regulamentOrdineInterioara: boolean | null
    manualPoliticiContabile: boolean | null
    adresaRevisal: boolean | null
    parolaITM: string | null
    depunereDeclaratiiOnline: boolean | null
    accesDosarFiscal: $Enums.DaNuNuECazul | null
    clientId: number | null
  }

  export type DetaliiCountAggregateOutputType = {
    id: number
    registruUC: number
    registruEvFiscala: number
    ofSpalareBani: number
    regulamentOrdineInterioara: number
    manualPoliticiContabile: number
    adresaRevisal: number
    parolaITM: number
    depunereDeclaratiiOnline: number
    accesDosarFiscal: number
    clientId: number
    _all: number
  }


  export type DetaliiAvgAggregateInputType = {
    id?: true
    clientId?: true
  }

  export type DetaliiSumAggregateInputType = {
    id?: true
    clientId?: true
  }

  export type DetaliiMinAggregateInputType = {
    id?: true
    registruUC?: true
    registruEvFiscala?: true
    ofSpalareBani?: true
    regulamentOrdineInterioara?: true
    manualPoliticiContabile?: true
    adresaRevisal?: true
    parolaITM?: true
    depunereDeclaratiiOnline?: true
    accesDosarFiscal?: true
    clientId?: true
  }

  export type DetaliiMaxAggregateInputType = {
    id?: true
    registruUC?: true
    registruEvFiscala?: true
    ofSpalareBani?: true
    regulamentOrdineInterioara?: true
    manualPoliticiContabile?: true
    adresaRevisal?: true
    parolaITM?: true
    depunereDeclaratiiOnline?: true
    accesDosarFiscal?: true
    clientId?: true
  }

  export type DetaliiCountAggregateInputType = {
    id?: true
    registruUC?: true
    registruEvFiscala?: true
    ofSpalareBani?: true
    regulamentOrdineInterioara?: true
    manualPoliticiContabile?: true
    adresaRevisal?: true
    parolaITM?: true
    depunereDeclaratiiOnline?: true
    accesDosarFiscal?: true
    clientId?: true
    _all?: true
  }

  export type DetaliiAggregateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Filter which Detalii to aggregate.
     */
    where?: DetaliiWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Detaliis to fetch.
     */
    orderBy?: DetaliiOrderByWithRelationInput | DetaliiOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the start position
     */
    cursor?: DetaliiWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Detaliis from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Detaliis.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Count returned Detaliis
    **/
    _count?: true | DetaliiCountAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to average
    **/
    _avg?: DetaliiAvgAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to sum
    **/
    _sum?: DetaliiSumAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to find the minimum value
    **/
    _min?: DetaliiMinAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to find the maximum value
    **/
    _max?: DetaliiMaxAggregateInputType
  }

  export type GetDetaliiAggregateType<T extends DetaliiAggregateArgs> = {
        [P in keyof T & keyof AggregateDetalii]: P extends '_count' | 'count'
      ? T[P] extends true
        ? number
        : GetScalarType<T[P], AggregateDetalii[P]>
      : GetScalarType<T[P], AggregateDetalii[P]>
  }




  export type DetaliiGroupByArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    where?: DetaliiWhereInput
    orderBy?: DetaliiOrderByWithAggregationInput | DetaliiOrderByWithAggregationInput[]
    by: DetaliiScalarFieldEnum[] | DetaliiScalarFieldEnum
    having?: DetaliiScalarWhereWithAggregatesInput
    take?: number
    skip?: number
    _count?: DetaliiCountAggregateInputType | true
    _avg?: DetaliiAvgAggregateInputType
    _sum?: DetaliiSumAggregateInputType
    _min?: DetaliiMinAggregateInputType
    _max?: DetaliiMaxAggregateInputType
  }

  export type DetaliiGroupByOutputType = {
    id: number
    registruUC: boolean
    registruEvFiscala: $Enums.DaNuNuECazul
    ofSpalareBani: boolean
    regulamentOrdineInterioara: boolean
    manualPoliticiContabile: boolean
    adresaRevisal: boolean
    parolaITM: string | null
    depunereDeclaratiiOnline: boolean
    accesDosarFiscal: $Enums.DaNuNuECazul
    clientId: number
    _count: DetaliiCountAggregateOutputType | null
    _avg: DetaliiAvgAggregateOutputType | null
    _sum: DetaliiSumAggregateOutputType | null
    _min: DetaliiMinAggregateOutputType | null
    _max: DetaliiMaxAggregateOutputType | null
  }

  type GetDetaliiGroupByPayload<T extends DetaliiGroupByArgs> = Prisma.PrismaPromise<
    Array<
      PickEnumerable<DetaliiGroupByOutputType, T['by']> &
        {
          [P in ((keyof T) & (keyof DetaliiGroupByOutputType))]: P extends '_count'
            ? T[P] extends boolean
              ? number
              : GetScalarType<T[P], DetaliiGroupByOutputType[P]>
            : GetScalarType<T[P], DetaliiGroupByOutputType[P]>
        }
      >
    >


  export type DetaliiSelect<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    registruUC?: boolean
    registruEvFiscala?: boolean
    ofSpalareBani?: boolean
    regulamentOrdineInterioara?: boolean
    manualPoliticiContabile?: boolean
    adresaRevisal?: boolean
    parolaITM?: boolean
    depunereDeclaratiiOnline?: boolean
    accesDosarFiscal?: boolean
    clientId?: boolean
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["detalii"]>

  export type DetaliiSelectCreateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    registruUC?: boolean
    registruEvFiscala?: boolean
    ofSpalareBani?: boolean
    regulamentOrdineInterioara?: boolean
    manualPoliticiContabile?: boolean
    adresaRevisal?: boolean
    parolaITM?: boolean
    depunereDeclaratiiOnline?: boolean
    accesDosarFiscal?: boolean
    clientId?: boolean
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["detalii"]>

  export type DetaliiSelectUpdateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    registruUC?: boolean
    registruEvFiscala?: boolean
    ofSpalareBani?: boolean
    regulamentOrdineInterioara?: boolean
    manualPoliticiContabile?: boolean
    adresaRevisal?: boolean
    parolaITM?: boolean
    depunereDeclaratiiOnline?: boolean
    accesDosarFiscal?: boolean
    clientId?: boolean
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["detalii"]>

  export type DetaliiSelectScalar = {
    id?: boolean
    registruUC?: boolean
    registruEvFiscala?: boolean
    ofSpalareBani?: boolean
    regulamentOrdineInterioara?: boolean
    manualPoliticiContabile?: boolean
    adresaRevisal?: boolean
    parolaITM?: boolean
    depunereDeclaratiiOnline?: boolean
    accesDosarFiscal?: boolean
    clientId?: boolean
  }

  export type DetaliiOmit<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetOmit<"id" | "registruUC" | "registruEvFiscala" | "ofSpalareBani" | "regulamentOrdineInterioara" | "manualPoliticiContabile" | "adresaRevisal" | "parolaITM" | "depunereDeclaratiiOnline" | "accesDosarFiscal" | "clientId", ExtArgs["result"]["detalii"]>
  export type DetaliiInclude<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }
  export type DetaliiIncludeCreateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }
  export type DetaliiIncludeUpdateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }

  export type $DetaliiPayload<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    name: "Detalii"
    objects: {
      client: Prisma.$ClientPayload<ExtArgs>
    }
    scalars: $Extensions.GetPayloadResult<{
      id: number
      registruUC: boolean
      registruEvFiscala: $Enums.DaNuNuECazul
      ofSpalareBani: boolean
      regulamentOrdineInterioara: boolean
      manualPoliticiContabile: boolean
      adresaRevisal: boolean
      parolaITM: string | null
      depunereDeclaratiiOnline: boolean
      accesDosarFiscal: $Enums.DaNuNuECazul
      clientId: number
    }, ExtArgs["result"]["detalii"]>
    composites: {}
  }

  type DetaliiGetPayload<S extends boolean | null | undefined | DetaliiDefaultArgs> = $Result.GetResult<Prisma.$DetaliiPayload, S>

  type DetaliiCountArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> =
    Omit<DetaliiFindManyArgs, 'select' | 'include' | 'distinct' | 'omit'> & {
      select?: DetaliiCountAggregateInputType | true
    }

  export interface DetaliiDelegate<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs, GlobalOmitOptions = {}> {
    [K: symbol]: { types: Prisma.TypeMap<ExtArgs>['model']['Detalii'], meta: { name: 'Detalii' } }
    /**
     * Find zero or one Detalii that matches the filter.
     * @param {DetaliiFindUniqueArgs} args - Arguments to find a Detalii
     * @example
     * // Get one Detalii
     * const detalii = await prisma.detalii.findUnique({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findUnique<T extends DetaliiFindUniqueArgs>(args: SelectSubset<T, DetaliiFindUniqueArgs<ExtArgs>>): Prisma__DetaliiClient<$Result.GetResult<Prisma.$DetaliiPayload<ExtArgs>, T, "findUnique", GlobalOmitOptions> | null, null, ExtArgs, GlobalOmitOptions>

    /**
     * Find one Detalii that matches the filter or throw an error with `error.code='P2025'`
     * if no matches were found.
     * @param {DetaliiFindUniqueOrThrowArgs} args - Arguments to find a Detalii
     * @example
     * // Get one Detalii
     * const detalii = await prisma.detalii.findUniqueOrThrow({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findUniqueOrThrow<T extends DetaliiFindUniqueOrThrowArgs>(args: SelectSubset<T, DetaliiFindUniqueOrThrowArgs<ExtArgs>>): Prisma__DetaliiClient<$Result.GetResult<Prisma.$DetaliiPayload<ExtArgs>, T, "findUniqueOrThrow", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Find the first Detalii that matches the filter.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {DetaliiFindFirstArgs} args - Arguments to find a Detalii
     * @example
     * // Get one Detalii
     * const detalii = await prisma.detalii.findFirst({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findFirst<T extends DetaliiFindFirstArgs>(args?: SelectSubset<T, DetaliiFindFirstArgs<ExtArgs>>): Prisma__DetaliiClient<$Result.GetResult<Prisma.$DetaliiPayload<ExtArgs>, T, "findFirst", GlobalOmitOptions> | null, null, ExtArgs, GlobalOmitOptions>

    /**
     * Find the first Detalii that matches the filter or
     * throw `PrismaKnownClientError` with `P2025` code if no matches were found.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {DetaliiFindFirstOrThrowArgs} args - Arguments to find a Detalii
     * @example
     * // Get one Detalii
     * const detalii = await prisma.detalii.findFirstOrThrow({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findFirstOrThrow<T extends DetaliiFindFirstOrThrowArgs>(args?: SelectSubset<T, DetaliiFindFirstOrThrowArgs<ExtArgs>>): Prisma__DetaliiClient<$Result.GetResult<Prisma.$DetaliiPayload<ExtArgs>, T, "findFirstOrThrow", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Find zero or more Detaliis that matches the filter.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {DetaliiFindManyArgs} args - Arguments to filter and select certain fields only.
     * @example
     * // Get all Detaliis
     * const detaliis = await prisma.detalii.findMany()
     * 
     * // Get first 10 Detaliis
     * const detaliis = await prisma.detalii.findMany({ take: 10 })
     * 
     * // Only select the `id`
     * const detaliiWithIdOnly = await prisma.detalii.findMany({ select: { id: true } })
     * 
     */
    findMany<T extends DetaliiFindManyArgs>(args?: SelectSubset<T, DetaliiFindManyArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$DetaliiPayload<ExtArgs>, T, "findMany", GlobalOmitOptions>>

    /**
     * Create a Detalii.
     * @param {DetaliiCreateArgs} args - Arguments to create a Detalii.
     * @example
     * // Create one Detalii
     * const Detalii = await prisma.detalii.create({
     *   data: {
     *     // ... data to create a Detalii
     *   }
     * })
     * 
     */
    create<T extends DetaliiCreateArgs>(args: SelectSubset<T, DetaliiCreateArgs<ExtArgs>>): Prisma__DetaliiClient<$Result.GetResult<Prisma.$DetaliiPayload<ExtArgs>, T, "create", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Create many Detaliis.
     * @param {DetaliiCreateManyArgs} args - Arguments to create many Detaliis.
     * @example
     * // Create many Detaliis
     * const detalii = await prisma.detalii.createMany({
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     *     
     */
    createMany<T extends DetaliiCreateManyArgs>(args?: SelectSubset<T, DetaliiCreateManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Create many Detaliis and returns the data saved in the database.
     * @param {DetaliiCreateManyAndReturnArgs} args - Arguments to create many Detaliis.
     * @example
     * // Create many Detaliis
     * const detalii = await prisma.detalii.createManyAndReturn({
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * 
     * // Create many Detaliis and only return the `id`
     * const detaliiWithIdOnly = await prisma.detalii.createManyAndReturn({
     *   select: { id: true },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * 
     */
    createManyAndReturn<T extends DetaliiCreateManyAndReturnArgs>(args?: SelectSubset<T, DetaliiCreateManyAndReturnArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$DetaliiPayload<ExtArgs>, T, "createManyAndReturn", GlobalOmitOptions>>

    /**
     * Delete a Detalii.
     * @param {DetaliiDeleteArgs} args - Arguments to delete one Detalii.
     * @example
     * // Delete one Detalii
     * const Detalii = await prisma.detalii.delete({
     *   where: {
     *     // ... filter to delete one Detalii
     *   }
     * })
     * 
     */
    delete<T extends DetaliiDeleteArgs>(args: SelectSubset<T, DetaliiDeleteArgs<ExtArgs>>): Prisma__DetaliiClient<$Result.GetResult<Prisma.$DetaliiPayload<ExtArgs>, T, "delete", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Update one Detalii.
     * @param {DetaliiUpdateArgs} args - Arguments to update one Detalii.
     * @example
     * // Update one Detalii
     * const detalii = await prisma.detalii.update({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: {
     *     // ... provide data here
     *   }
     * })
     * 
     */
    update<T extends DetaliiUpdateArgs>(args: SelectSubset<T, DetaliiUpdateArgs<ExtArgs>>): Prisma__DetaliiClient<$Result.GetResult<Prisma.$DetaliiPayload<ExtArgs>, T, "update", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Delete zero or more Detaliis.
     * @param {DetaliiDeleteManyArgs} args - Arguments to filter Detaliis to delete.
     * @example
     * // Delete a few Detaliis
     * const { count } = await prisma.detalii.deleteMany({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     * 
     */
    deleteMany<T extends DetaliiDeleteManyArgs>(args?: SelectSubset<T, DetaliiDeleteManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Update zero or more Detaliis.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {DetaliiUpdateManyArgs} args - Arguments to update one or more rows.
     * @example
     * // Update many Detaliis
     * const detalii = await prisma.detalii.updateMany({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: {
     *     // ... provide data here
     *   }
     * })
     * 
     */
    updateMany<T extends DetaliiUpdateManyArgs>(args: SelectSubset<T, DetaliiUpdateManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Update zero or more Detaliis and returns the data updated in the database.
     * @param {DetaliiUpdateManyAndReturnArgs} args - Arguments to update many Detaliis.
     * @example
     * // Update many Detaliis
     * const detalii = await prisma.detalii.updateManyAndReturn({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * 
     * // Update zero or more Detaliis and only return the `id`
     * const detaliiWithIdOnly = await prisma.detalii.updateManyAndReturn({
     *   select: { id: true },
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * 
     */
    updateManyAndReturn<T extends DetaliiUpdateManyAndReturnArgs>(args: SelectSubset<T, DetaliiUpdateManyAndReturnArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$DetaliiPayload<ExtArgs>, T, "updateManyAndReturn", GlobalOmitOptions>>

    /**
     * Create or update one Detalii.
     * @param {DetaliiUpsertArgs} args - Arguments to update or create a Detalii.
     * @example
     * // Update or create a Detalii
     * const detalii = await prisma.detalii.upsert({
     *   create: {
     *     // ... data to create a Detalii
     *   },
     *   update: {
     *     // ... in case it already exists, update
     *   },
     *   where: {
     *     // ... the filter for the Detalii we want to update
     *   }
     * })
     */
    upsert<T extends DetaliiUpsertArgs>(args: SelectSubset<T, DetaliiUpsertArgs<ExtArgs>>): Prisma__DetaliiClient<$Result.GetResult<Prisma.$DetaliiPayload<ExtArgs>, T, "upsert", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>


    /**
     * Count the number of Detaliis.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {DetaliiCountArgs} args - Arguments to filter Detaliis to count.
     * @example
     * // Count the number of Detaliis
     * const count = await prisma.detalii.count({
     *   where: {
     *     // ... the filter for the Detaliis we want to count
     *   }
     * })
    **/
    count<T extends DetaliiCountArgs>(
      args?: Subset<T, DetaliiCountArgs>,
    ): Prisma.PrismaPromise<
      T extends $Utils.Record<'select', any>
        ? T['select'] extends true
          ? number
          : GetScalarType<T['select'], DetaliiCountAggregateOutputType>
        : number
    >

    /**
     * Allows you to perform aggregations operations on a Detalii.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {DetaliiAggregateArgs} args - Select which aggregations you would like to apply and on what fields.
     * @example
     * // Ordered by age ascending
     * // Where email contains prisma.io
     * // Limited to the 10 users
     * const aggregations = await prisma.user.aggregate({
     *   _avg: {
     *     age: true,
     *   },
     *   where: {
     *     email: {
     *       contains: "prisma.io",
     *     },
     *   },
     *   orderBy: {
     *     age: "asc",
     *   },
     *   take: 10,
     * })
    **/
    aggregate<T extends DetaliiAggregateArgs>(args: Subset<T, DetaliiAggregateArgs>): Prisma.PrismaPromise<GetDetaliiAggregateType<T>>

    /**
     * Group by Detalii.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {DetaliiGroupByArgs} args - Group by arguments.
     * @example
     * // Group by city, order by createdAt, get count
     * const result = await prisma.user.groupBy({
     *   by: ['city', 'createdAt'],
     *   orderBy: {
     *     createdAt: true
     *   },
     *   _count: {
     *     _all: true
     *   },
     * })
     * 
    **/
    groupBy<
      T extends DetaliiGroupByArgs,
      HasSelectOrTake extends Or<
        Extends<'skip', Keys<T>>,
        Extends<'take', Keys<T>>
      >,
      OrderByArg extends True extends HasSelectOrTake
        ? { orderBy: DetaliiGroupByArgs['orderBy'] }
        : { orderBy?: DetaliiGroupByArgs['orderBy'] },
      OrderFields extends ExcludeUnderscoreKeys<Keys<MaybeTupleToUnion<T['orderBy']>>>,
      ByFields extends MaybeTupleToUnion<T['by']>,
      ByValid extends Has<ByFields, OrderFields>,
      HavingFields extends GetHavingFields<T['having']>,
      HavingValid extends Has<ByFields, HavingFields>,
      ByEmpty extends T['by'] extends never[] ? True : False,
      InputErrors extends ByEmpty extends True
      ? `Error: "by" must not be empty.`
      : HavingValid extends False
      ? {
          [P in HavingFields]: P extends ByFields
            ? never
            : P extends string
            ? `Error: Field "${P}" used in "having" needs to be provided in "by".`
            : [
                Error,
                'Field ',
                P,
                ` in "having" needs to be provided in "by"`,
              ]
        }[HavingFields]
      : 'take' extends Keys<T>
      ? 'orderBy' extends Keys<T>
        ? ByValid extends True
          ? {}
          : {
              [P in OrderFields]: P extends ByFields
                ? never
                : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
            }[OrderFields]
        : 'Error: If you provide "take", you also need to provide "orderBy"'
      : 'skip' extends Keys<T>
      ? 'orderBy' extends Keys<T>
        ? ByValid extends True
          ? {}
          : {
              [P in OrderFields]: P extends ByFields
                ? never
                : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
            }[OrderFields]
        : 'Error: If you provide "skip", you also need to provide "orderBy"'
      : ByValid extends True
      ? {}
      : {
          [P in OrderFields]: P extends ByFields
            ? never
            : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
        }[OrderFields]
    >(args: SubsetIntersection<T, DetaliiGroupByArgs, OrderByArg> & InputErrors): {} extends InputErrors ? GetDetaliiGroupByPayload<T> : Prisma.PrismaPromise<InputErrors>
  /**
   * Fields of the Detalii model
   */
  readonly fields: DetaliiFieldRefs;
  }

  /**
   * The delegate class that acts as a "Promise-like" for Detalii.
   * Why is this prefixed with `Prisma__`?
   * Because we want to prevent naming conflicts as mentioned in
   * https://github.com/prisma/prisma-client-js/issues/707
   */
  export interface Prisma__DetaliiClient<T, Null = never, ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs, GlobalOmitOptions = {}> extends Prisma.PrismaPromise<T> {
    readonly [Symbol.toStringTag]: "PrismaPromise"
    client<T extends ClientDefaultArgs<ExtArgs> = {}>(args?: Subset<T, ClientDefaultArgs<ExtArgs>>): Prisma__ClientClient<$Result.GetResult<Prisma.$ClientPayload<ExtArgs>, T, "findUniqueOrThrow", GlobalOmitOptions> | Null, Null, ExtArgs, GlobalOmitOptions>
    /**
     * Attaches callbacks for the resolution and/or rejection of the Promise.
     * @param onfulfilled The callback to execute when the Promise is resolved.
     * @param onrejected The callback to execute when the Promise is rejected.
     * @returns A Promise for the completion of which ever callback is executed.
     */
    then<TResult1 = T, TResult2 = never>(onfulfilled?: ((value: T) => TResult1 | PromiseLike<TResult1>) | undefined | null, onrejected?: ((reason: any) => TResult2 | PromiseLike<TResult2>) | undefined | null): $Utils.JsPromise<TResult1 | TResult2>
    /**
     * Attaches a callback for only the rejection of the Promise.
     * @param onrejected The callback to execute when the Promise is rejected.
     * @returns A Promise for the completion of the callback.
     */
    catch<TResult = never>(onrejected?: ((reason: any) => TResult | PromiseLike<TResult>) | undefined | null): $Utils.JsPromise<T | TResult>
    /**
     * Attaches a callback that is invoked when the Promise is settled (fulfilled or rejected). The
     * resolved value cannot be modified from the callback.
     * @param onfinally The callback to execute when the Promise is settled (fulfilled or rejected).
     * @returns A Promise for the completion of the callback.
     */
    finally(onfinally?: (() => void) | undefined | null): $Utils.JsPromise<T>
  }




  /**
   * Fields of the Detalii model
   */
  interface DetaliiFieldRefs {
    readonly id: FieldRef<"Detalii", 'Int'>
    readonly registruUC: FieldRef<"Detalii", 'Boolean'>
    readonly registruEvFiscala: FieldRef<"Detalii", 'DaNuNuECazul'>
    readonly ofSpalareBani: FieldRef<"Detalii", 'Boolean'>
    readonly regulamentOrdineInterioara: FieldRef<"Detalii", 'Boolean'>
    readonly manualPoliticiContabile: FieldRef<"Detalii", 'Boolean'>
    readonly adresaRevisal: FieldRef<"Detalii", 'Boolean'>
    readonly parolaITM: FieldRef<"Detalii", 'String'>
    readonly depunereDeclaratiiOnline: FieldRef<"Detalii", 'Boolean'>
    readonly accesDosarFiscal: FieldRef<"Detalii", 'DaNuNuECazul'>
    readonly clientId: FieldRef<"Detalii", 'Int'>
  }
    

  // Custom InputTypes
  /**
   * Detalii findUnique
   */
  export type DetaliiFindUniqueArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Detalii
     */
    select?: DetaliiSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Detalii
     */
    omit?: DetaliiOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: DetaliiInclude<ExtArgs> | null
    /**
     * Filter, which Detalii to fetch.
     */
    where: DetaliiWhereUniqueInput
  }

  /**
   * Detalii findUniqueOrThrow
   */
  export type DetaliiFindUniqueOrThrowArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Detalii
     */
    select?: DetaliiSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Detalii
     */
    omit?: DetaliiOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: DetaliiInclude<ExtArgs> | null
    /**
     * Filter, which Detalii to fetch.
     */
    where: DetaliiWhereUniqueInput
  }

  /**
   * Detalii findFirst
   */
  export type DetaliiFindFirstArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Detalii
     */
    select?: DetaliiSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Detalii
     */
    omit?: DetaliiOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: DetaliiInclude<ExtArgs> | null
    /**
     * Filter, which Detalii to fetch.
     */
    where?: DetaliiWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Detaliis to fetch.
     */
    orderBy?: DetaliiOrderByWithRelationInput | DetaliiOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for searching for Detaliis.
     */
    cursor?: DetaliiWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Detaliis from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Detaliis.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/distinct Distinct Docs}
     * 
     * Filter by unique combinations of Detaliis.
     */
    distinct?: DetaliiScalarFieldEnum | DetaliiScalarFieldEnum[]
  }

  /**
   * Detalii findFirstOrThrow
   */
  export type DetaliiFindFirstOrThrowArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Detalii
     */
    select?: DetaliiSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Detalii
     */
    omit?: DetaliiOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: DetaliiInclude<ExtArgs> | null
    /**
     * Filter, which Detalii to fetch.
     */
    where?: DetaliiWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Detaliis to fetch.
     */
    orderBy?: DetaliiOrderByWithRelationInput | DetaliiOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for searching for Detaliis.
     */
    cursor?: DetaliiWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Detaliis from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Detaliis.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/distinct Distinct Docs}
     * 
     * Filter by unique combinations of Detaliis.
     */
    distinct?: DetaliiScalarFieldEnum | DetaliiScalarFieldEnum[]
  }

  /**
   * Detalii findMany
   */
  export type DetaliiFindManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Detalii
     */
    select?: DetaliiSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Detalii
     */
    omit?: DetaliiOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: DetaliiInclude<ExtArgs> | null
    /**
     * Filter, which Detaliis to fetch.
     */
    where?: DetaliiWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Detaliis to fetch.
     */
    orderBy?: DetaliiOrderByWithRelationInput | DetaliiOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for listing Detaliis.
     */
    cursor?: DetaliiWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Detaliis from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Detaliis.
     */
    skip?: number
    distinct?: DetaliiScalarFieldEnum | DetaliiScalarFieldEnum[]
  }

  /**
   * Detalii create
   */
  export type DetaliiCreateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Detalii
     */
    select?: DetaliiSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Detalii
     */
    omit?: DetaliiOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: DetaliiInclude<ExtArgs> | null
    /**
     * The data needed to create a Detalii.
     */
    data: XOR<DetaliiCreateInput, DetaliiUncheckedCreateInput>
  }

  /**
   * Detalii createMany
   */
  export type DetaliiCreateManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * The data used to create many Detaliis.
     */
    data: DetaliiCreateManyInput | DetaliiCreateManyInput[]
    skipDuplicates?: boolean
  }

  /**
   * Detalii createManyAndReturn
   */
  export type DetaliiCreateManyAndReturnArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Detalii
     */
    select?: DetaliiSelectCreateManyAndReturn<ExtArgs> | null
    /**
     * Omit specific fields from the Detalii
     */
    omit?: DetaliiOmit<ExtArgs> | null
    /**
     * The data used to create many Detaliis.
     */
    data: DetaliiCreateManyInput | DetaliiCreateManyInput[]
    skipDuplicates?: boolean
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: DetaliiIncludeCreateManyAndReturn<ExtArgs> | null
  }

  /**
   * Detalii update
   */
  export type DetaliiUpdateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Detalii
     */
    select?: DetaliiSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Detalii
     */
    omit?: DetaliiOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: DetaliiInclude<ExtArgs> | null
    /**
     * The data needed to update a Detalii.
     */
    data: XOR<DetaliiUpdateInput, DetaliiUncheckedUpdateInput>
    /**
     * Choose, which Detalii to update.
     */
    where: DetaliiWhereUniqueInput
  }

  /**
   * Detalii updateMany
   */
  export type DetaliiUpdateManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * The data used to update Detaliis.
     */
    data: XOR<DetaliiUpdateManyMutationInput, DetaliiUncheckedUpdateManyInput>
    /**
     * Filter which Detaliis to update
     */
    where?: DetaliiWhereInput
    /**
     * Limit how many Detaliis to update.
     */
    limit?: number
  }

  /**
   * Detalii updateManyAndReturn
   */
  export type DetaliiUpdateManyAndReturnArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Detalii
     */
    select?: DetaliiSelectUpdateManyAndReturn<ExtArgs> | null
    /**
     * Omit specific fields from the Detalii
     */
    omit?: DetaliiOmit<ExtArgs> | null
    /**
     * The data used to update Detaliis.
     */
    data: XOR<DetaliiUpdateManyMutationInput, DetaliiUncheckedUpdateManyInput>
    /**
     * Filter which Detaliis to update
     */
    where?: DetaliiWhereInput
    /**
     * Limit how many Detaliis to update.
     */
    limit?: number
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: DetaliiIncludeUpdateManyAndReturn<ExtArgs> | null
  }

  /**
   * Detalii upsert
   */
  export type DetaliiUpsertArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Detalii
     */
    select?: DetaliiSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Detalii
     */
    omit?: DetaliiOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: DetaliiInclude<ExtArgs> | null
    /**
     * The filter to search for the Detalii to update in case it exists.
     */
    where: DetaliiWhereUniqueInput
    /**
     * In case the Detalii found by the `where` argument doesn't exist, create a new Detalii with this data.
     */
    create: XOR<DetaliiCreateInput, DetaliiUncheckedCreateInput>
    /**
     * In case the Detalii was found with the provided `where` argument, update it with this data.
     */
    update: XOR<DetaliiUpdateInput, DetaliiUncheckedUpdateInput>
  }

  /**
   * Detalii delete
   */
  export type DetaliiDeleteArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Detalii
     */
    select?: DetaliiSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Detalii
     */
    omit?: DetaliiOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: DetaliiInclude<ExtArgs> | null
    /**
     * Filter which Detalii to delete.
     */
    where: DetaliiWhereUniqueInput
  }

  /**
   * Detalii deleteMany
   */
  export type DetaliiDeleteManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Filter which Detaliis to delete
     */
    where?: DetaliiWhereInput
    /**
     * Limit how many Detaliis to delete.
     */
    limit?: number
  }

  /**
   * Detalii without action
   */
  export type DetaliiDefaultArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Detalii
     */
    select?: DetaliiSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Detalii
     */
    omit?: DetaliiOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: DetaliiInclude<ExtArgs> | null
  }


  /**
   * Model PunctDeLucru
   */

  export type AggregatePunctDeLucru = {
    _count: PunctDeLucruCountAggregateOutputType | null
    _avg: PunctDeLucruAvgAggregateOutputType | null
    _sum: PunctDeLucruSumAggregateOutputType | null
    _min: PunctDeLucruMinAggregateOutputType | null
    _max: PunctDeLucruMaxAggregateOutputType | null
  }

  export type PunctDeLucruAvgAggregateOutputType = {
    id: number | null
    salariati: number | null
    clientId: number | null
  }

  export type PunctDeLucruSumAggregateOutputType = {
    id: number | null
    salariati: number | null
    clientId: number | null
  }

  export type PunctDeLucruMinAggregateOutputType = {
    id: number | null
    denumire: string | null
    deLa: Date | null
    panaLa: Date | null
    administratie: $Enums.Administratie | null
    registruUC: boolean | null
    salariati: number | null
    cui: string | null
    casaDeMarcat: boolean | null
    clientId: number | null
  }

  export type PunctDeLucruMaxAggregateOutputType = {
    id: number | null
    denumire: string | null
    deLa: Date | null
    panaLa: Date | null
    administratie: $Enums.Administratie | null
    registruUC: boolean | null
    salariati: number | null
    cui: string | null
    casaDeMarcat: boolean | null
    clientId: number | null
  }

  export type PunctDeLucruCountAggregateOutputType = {
    id: number
    denumire: number
    deLa: number
    panaLa: number
    administratie: number
    registruUC: number
    salariati: number
    cui: number
    casaDeMarcat: number
    clientId: number
    _all: number
  }


  export type PunctDeLucruAvgAggregateInputType = {
    id?: true
    salariati?: true
    clientId?: true
  }

  export type PunctDeLucruSumAggregateInputType = {
    id?: true
    salariati?: true
    clientId?: true
  }

  export type PunctDeLucruMinAggregateInputType = {
    id?: true
    denumire?: true
    deLa?: true
    panaLa?: true
    administratie?: true
    registruUC?: true
    salariati?: true
    cui?: true
    casaDeMarcat?: true
    clientId?: true
  }

  export type PunctDeLucruMaxAggregateInputType = {
    id?: true
    denumire?: true
    deLa?: true
    panaLa?: true
    administratie?: true
    registruUC?: true
    salariati?: true
    cui?: true
    casaDeMarcat?: true
    clientId?: true
  }

  export type PunctDeLucruCountAggregateInputType = {
    id?: true
    denumire?: true
    deLa?: true
    panaLa?: true
    administratie?: true
    registruUC?: true
    salariati?: true
    cui?: true
    casaDeMarcat?: true
    clientId?: true
    _all?: true
  }

  export type PunctDeLucruAggregateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Filter which PunctDeLucru to aggregate.
     */
    where?: PunctDeLucruWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of PunctDeLucrus to fetch.
     */
    orderBy?: PunctDeLucruOrderByWithRelationInput | PunctDeLucruOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the start position
     */
    cursor?: PunctDeLucruWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` PunctDeLucrus from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` PunctDeLucrus.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Count returned PunctDeLucrus
    **/
    _count?: true | PunctDeLucruCountAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to average
    **/
    _avg?: PunctDeLucruAvgAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to sum
    **/
    _sum?: PunctDeLucruSumAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to find the minimum value
    **/
    _min?: PunctDeLucruMinAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to find the maximum value
    **/
    _max?: PunctDeLucruMaxAggregateInputType
  }

  export type GetPunctDeLucruAggregateType<T extends PunctDeLucruAggregateArgs> = {
        [P in keyof T & keyof AggregatePunctDeLucru]: P extends '_count' | 'count'
      ? T[P] extends true
        ? number
        : GetScalarType<T[P], AggregatePunctDeLucru[P]>
      : GetScalarType<T[P], AggregatePunctDeLucru[P]>
  }




  export type PunctDeLucruGroupByArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    where?: PunctDeLucruWhereInput
    orderBy?: PunctDeLucruOrderByWithAggregationInput | PunctDeLucruOrderByWithAggregationInput[]
    by: PunctDeLucruScalarFieldEnum[] | PunctDeLucruScalarFieldEnum
    having?: PunctDeLucruScalarWhereWithAggregatesInput
    take?: number
    skip?: number
    _count?: PunctDeLucruCountAggregateInputType | true
    _avg?: PunctDeLucruAvgAggregateInputType
    _sum?: PunctDeLucruSumAggregateInputType
    _min?: PunctDeLucruMinAggregateInputType
    _max?: PunctDeLucruMaxAggregateInputType
  }

  export type PunctDeLucruGroupByOutputType = {
    id: number
    denumire: string
    deLa: Date
    panaLa: Date | null
    administratie: $Enums.Administratie
    registruUC: boolean
    salariati: number
    cui: string | null
    casaDeMarcat: boolean
    clientId: number
    _count: PunctDeLucruCountAggregateOutputType | null
    _avg: PunctDeLucruAvgAggregateOutputType | null
    _sum: PunctDeLucruSumAggregateOutputType | null
    _min: PunctDeLucruMinAggregateOutputType | null
    _max: PunctDeLucruMaxAggregateOutputType | null
  }

  type GetPunctDeLucruGroupByPayload<T extends PunctDeLucruGroupByArgs> = Prisma.PrismaPromise<
    Array<
      PickEnumerable<PunctDeLucruGroupByOutputType, T['by']> &
        {
          [P in ((keyof T) & (keyof PunctDeLucruGroupByOutputType))]: P extends '_count'
            ? T[P] extends boolean
              ? number
              : GetScalarType<T[P], PunctDeLucruGroupByOutputType[P]>
            : GetScalarType<T[P], PunctDeLucruGroupByOutputType[P]>
        }
      >
    >


  export type PunctDeLucruSelect<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    denumire?: boolean
    deLa?: boolean
    panaLa?: boolean
    administratie?: boolean
    registruUC?: boolean
    salariati?: boolean
    cui?: boolean
    casaDeMarcat?: boolean
    clientId?: boolean
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["punctDeLucru"]>

  export type PunctDeLucruSelectCreateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    denumire?: boolean
    deLa?: boolean
    panaLa?: boolean
    administratie?: boolean
    registruUC?: boolean
    salariati?: boolean
    cui?: boolean
    casaDeMarcat?: boolean
    clientId?: boolean
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["punctDeLucru"]>

  export type PunctDeLucruSelectUpdateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    denumire?: boolean
    deLa?: boolean
    panaLa?: boolean
    administratie?: boolean
    registruUC?: boolean
    salariati?: boolean
    cui?: boolean
    casaDeMarcat?: boolean
    clientId?: boolean
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["punctDeLucru"]>

  export type PunctDeLucruSelectScalar = {
    id?: boolean
    denumire?: boolean
    deLa?: boolean
    panaLa?: boolean
    administratie?: boolean
    registruUC?: boolean
    salariati?: boolean
    cui?: boolean
    casaDeMarcat?: boolean
    clientId?: boolean
  }

  export type PunctDeLucruOmit<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetOmit<"id" | "denumire" | "deLa" | "panaLa" | "administratie" | "registruUC" | "salariati" | "cui" | "casaDeMarcat" | "clientId", ExtArgs["result"]["punctDeLucru"]>
  export type PunctDeLucruInclude<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }
  export type PunctDeLucruIncludeCreateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }
  export type PunctDeLucruIncludeUpdateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }

  export type $PunctDeLucruPayload<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    name: "PunctDeLucru"
    objects: {
      client: Prisma.$ClientPayload<ExtArgs>
    }
    scalars: $Extensions.GetPayloadResult<{
      id: number
      denumire: string
      deLa: Date
      panaLa: Date | null
      administratie: $Enums.Administratie
      registruUC: boolean
      salariati: number
      cui: string | null
      casaDeMarcat: boolean
      clientId: number
    }, ExtArgs["result"]["punctDeLucru"]>
    composites: {}
  }

  type PunctDeLucruGetPayload<S extends boolean | null | undefined | PunctDeLucruDefaultArgs> = $Result.GetResult<Prisma.$PunctDeLucruPayload, S>

  type PunctDeLucruCountArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> =
    Omit<PunctDeLucruFindManyArgs, 'select' | 'include' | 'distinct' | 'omit'> & {
      select?: PunctDeLucruCountAggregateInputType | true
    }

  export interface PunctDeLucruDelegate<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs, GlobalOmitOptions = {}> {
    [K: symbol]: { types: Prisma.TypeMap<ExtArgs>['model']['PunctDeLucru'], meta: { name: 'PunctDeLucru' } }
    /**
     * Find zero or one PunctDeLucru that matches the filter.
     * @param {PunctDeLucruFindUniqueArgs} args - Arguments to find a PunctDeLucru
     * @example
     * // Get one PunctDeLucru
     * const punctDeLucru = await prisma.punctDeLucru.findUnique({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findUnique<T extends PunctDeLucruFindUniqueArgs>(args: SelectSubset<T, PunctDeLucruFindUniqueArgs<ExtArgs>>): Prisma__PunctDeLucruClient<$Result.GetResult<Prisma.$PunctDeLucruPayload<ExtArgs>, T, "findUnique", GlobalOmitOptions> | null, null, ExtArgs, GlobalOmitOptions>

    /**
     * Find one PunctDeLucru that matches the filter or throw an error with `error.code='P2025'`
     * if no matches were found.
     * @param {PunctDeLucruFindUniqueOrThrowArgs} args - Arguments to find a PunctDeLucru
     * @example
     * // Get one PunctDeLucru
     * const punctDeLucru = await prisma.punctDeLucru.findUniqueOrThrow({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findUniqueOrThrow<T extends PunctDeLucruFindUniqueOrThrowArgs>(args: SelectSubset<T, PunctDeLucruFindUniqueOrThrowArgs<ExtArgs>>): Prisma__PunctDeLucruClient<$Result.GetResult<Prisma.$PunctDeLucruPayload<ExtArgs>, T, "findUniqueOrThrow", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Find the first PunctDeLucru that matches the filter.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {PunctDeLucruFindFirstArgs} args - Arguments to find a PunctDeLucru
     * @example
     * // Get one PunctDeLucru
     * const punctDeLucru = await prisma.punctDeLucru.findFirst({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findFirst<T extends PunctDeLucruFindFirstArgs>(args?: SelectSubset<T, PunctDeLucruFindFirstArgs<ExtArgs>>): Prisma__PunctDeLucruClient<$Result.GetResult<Prisma.$PunctDeLucruPayload<ExtArgs>, T, "findFirst", GlobalOmitOptions> | null, null, ExtArgs, GlobalOmitOptions>

    /**
     * Find the first PunctDeLucru that matches the filter or
     * throw `PrismaKnownClientError` with `P2025` code if no matches were found.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {PunctDeLucruFindFirstOrThrowArgs} args - Arguments to find a PunctDeLucru
     * @example
     * // Get one PunctDeLucru
     * const punctDeLucru = await prisma.punctDeLucru.findFirstOrThrow({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findFirstOrThrow<T extends PunctDeLucruFindFirstOrThrowArgs>(args?: SelectSubset<T, PunctDeLucruFindFirstOrThrowArgs<ExtArgs>>): Prisma__PunctDeLucruClient<$Result.GetResult<Prisma.$PunctDeLucruPayload<ExtArgs>, T, "findFirstOrThrow", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Find zero or more PunctDeLucrus that matches the filter.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {PunctDeLucruFindManyArgs} args - Arguments to filter and select certain fields only.
     * @example
     * // Get all PunctDeLucrus
     * const punctDeLucrus = await prisma.punctDeLucru.findMany()
     * 
     * // Get first 10 PunctDeLucrus
     * const punctDeLucrus = await prisma.punctDeLucru.findMany({ take: 10 })
     * 
     * // Only select the `id`
     * const punctDeLucruWithIdOnly = await prisma.punctDeLucru.findMany({ select: { id: true } })
     * 
     */
    findMany<T extends PunctDeLucruFindManyArgs>(args?: SelectSubset<T, PunctDeLucruFindManyArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$PunctDeLucruPayload<ExtArgs>, T, "findMany", GlobalOmitOptions>>

    /**
     * Create a PunctDeLucru.
     * @param {PunctDeLucruCreateArgs} args - Arguments to create a PunctDeLucru.
     * @example
     * // Create one PunctDeLucru
     * const PunctDeLucru = await prisma.punctDeLucru.create({
     *   data: {
     *     // ... data to create a PunctDeLucru
     *   }
     * })
     * 
     */
    create<T extends PunctDeLucruCreateArgs>(args: SelectSubset<T, PunctDeLucruCreateArgs<ExtArgs>>): Prisma__PunctDeLucruClient<$Result.GetResult<Prisma.$PunctDeLucruPayload<ExtArgs>, T, "create", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Create many PunctDeLucrus.
     * @param {PunctDeLucruCreateManyArgs} args - Arguments to create many PunctDeLucrus.
     * @example
     * // Create many PunctDeLucrus
     * const punctDeLucru = await prisma.punctDeLucru.createMany({
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     *     
     */
    createMany<T extends PunctDeLucruCreateManyArgs>(args?: SelectSubset<T, PunctDeLucruCreateManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Create many PunctDeLucrus and returns the data saved in the database.
     * @param {PunctDeLucruCreateManyAndReturnArgs} args - Arguments to create many PunctDeLucrus.
     * @example
     * // Create many PunctDeLucrus
     * const punctDeLucru = await prisma.punctDeLucru.createManyAndReturn({
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * 
     * // Create many PunctDeLucrus and only return the `id`
     * const punctDeLucruWithIdOnly = await prisma.punctDeLucru.createManyAndReturn({
     *   select: { id: true },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * 
     */
    createManyAndReturn<T extends PunctDeLucruCreateManyAndReturnArgs>(args?: SelectSubset<T, PunctDeLucruCreateManyAndReturnArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$PunctDeLucruPayload<ExtArgs>, T, "createManyAndReturn", GlobalOmitOptions>>

    /**
     * Delete a PunctDeLucru.
     * @param {PunctDeLucruDeleteArgs} args - Arguments to delete one PunctDeLucru.
     * @example
     * // Delete one PunctDeLucru
     * const PunctDeLucru = await prisma.punctDeLucru.delete({
     *   where: {
     *     // ... filter to delete one PunctDeLucru
     *   }
     * })
     * 
     */
    delete<T extends PunctDeLucruDeleteArgs>(args: SelectSubset<T, PunctDeLucruDeleteArgs<ExtArgs>>): Prisma__PunctDeLucruClient<$Result.GetResult<Prisma.$PunctDeLucruPayload<ExtArgs>, T, "delete", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Update one PunctDeLucru.
     * @param {PunctDeLucruUpdateArgs} args - Arguments to update one PunctDeLucru.
     * @example
     * // Update one PunctDeLucru
     * const punctDeLucru = await prisma.punctDeLucru.update({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: {
     *     // ... provide data here
     *   }
     * })
     * 
     */
    update<T extends PunctDeLucruUpdateArgs>(args: SelectSubset<T, PunctDeLucruUpdateArgs<ExtArgs>>): Prisma__PunctDeLucruClient<$Result.GetResult<Prisma.$PunctDeLucruPayload<ExtArgs>, T, "update", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Delete zero or more PunctDeLucrus.
     * @param {PunctDeLucruDeleteManyArgs} args - Arguments to filter PunctDeLucrus to delete.
     * @example
     * // Delete a few PunctDeLucrus
     * const { count } = await prisma.punctDeLucru.deleteMany({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     * 
     */
    deleteMany<T extends PunctDeLucruDeleteManyArgs>(args?: SelectSubset<T, PunctDeLucruDeleteManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Update zero or more PunctDeLucrus.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {PunctDeLucruUpdateManyArgs} args - Arguments to update one or more rows.
     * @example
     * // Update many PunctDeLucrus
     * const punctDeLucru = await prisma.punctDeLucru.updateMany({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: {
     *     // ... provide data here
     *   }
     * })
     * 
     */
    updateMany<T extends PunctDeLucruUpdateManyArgs>(args: SelectSubset<T, PunctDeLucruUpdateManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Update zero or more PunctDeLucrus and returns the data updated in the database.
     * @param {PunctDeLucruUpdateManyAndReturnArgs} args - Arguments to update many PunctDeLucrus.
     * @example
     * // Update many PunctDeLucrus
     * const punctDeLucru = await prisma.punctDeLucru.updateManyAndReturn({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * 
     * // Update zero or more PunctDeLucrus and only return the `id`
     * const punctDeLucruWithIdOnly = await prisma.punctDeLucru.updateManyAndReturn({
     *   select: { id: true },
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * 
     */
    updateManyAndReturn<T extends PunctDeLucruUpdateManyAndReturnArgs>(args: SelectSubset<T, PunctDeLucruUpdateManyAndReturnArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$PunctDeLucruPayload<ExtArgs>, T, "updateManyAndReturn", GlobalOmitOptions>>

    /**
     * Create or update one PunctDeLucru.
     * @param {PunctDeLucruUpsertArgs} args - Arguments to update or create a PunctDeLucru.
     * @example
     * // Update or create a PunctDeLucru
     * const punctDeLucru = await prisma.punctDeLucru.upsert({
     *   create: {
     *     // ... data to create a PunctDeLucru
     *   },
     *   update: {
     *     // ... in case it already exists, update
     *   },
     *   where: {
     *     // ... the filter for the PunctDeLucru we want to update
     *   }
     * })
     */
    upsert<T extends PunctDeLucruUpsertArgs>(args: SelectSubset<T, PunctDeLucruUpsertArgs<ExtArgs>>): Prisma__PunctDeLucruClient<$Result.GetResult<Prisma.$PunctDeLucruPayload<ExtArgs>, T, "upsert", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>


    /**
     * Count the number of PunctDeLucrus.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {PunctDeLucruCountArgs} args - Arguments to filter PunctDeLucrus to count.
     * @example
     * // Count the number of PunctDeLucrus
     * const count = await prisma.punctDeLucru.count({
     *   where: {
     *     // ... the filter for the PunctDeLucrus we want to count
     *   }
     * })
    **/
    count<T extends PunctDeLucruCountArgs>(
      args?: Subset<T, PunctDeLucruCountArgs>,
    ): Prisma.PrismaPromise<
      T extends $Utils.Record<'select', any>
        ? T['select'] extends true
          ? number
          : GetScalarType<T['select'], PunctDeLucruCountAggregateOutputType>
        : number
    >

    /**
     * Allows you to perform aggregations operations on a PunctDeLucru.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {PunctDeLucruAggregateArgs} args - Select which aggregations you would like to apply and on what fields.
     * @example
     * // Ordered by age ascending
     * // Where email contains prisma.io
     * // Limited to the 10 users
     * const aggregations = await prisma.user.aggregate({
     *   _avg: {
     *     age: true,
     *   },
     *   where: {
     *     email: {
     *       contains: "prisma.io",
     *     },
     *   },
     *   orderBy: {
     *     age: "asc",
     *   },
     *   take: 10,
     * })
    **/
    aggregate<T extends PunctDeLucruAggregateArgs>(args: Subset<T, PunctDeLucruAggregateArgs>): Prisma.PrismaPromise<GetPunctDeLucruAggregateType<T>>

    /**
     * Group by PunctDeLucru.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {PunctDeLucruGroupByArgs} args - Group by arguments.
     * @example
     * // Group by city, order by createdAt, get count
     * const result = await prisma.user.groupBy({
     *   by: ['city', 'createdAt'],
     *   orderBy: {
     *     createdAt: true
     *   },
     *   _count: {
     *     _all: true
     *   },
     * })
     * 
    **/
    groupBy<
      T extends PunctDeLucruGroupByArgs,
      HasSelectOrTake extends Or<
        Extends<'skip', Keys<T>>,
        Extends<'take', Keys<T>>
      >,
      OrderByArg extends True extends HasSelectOrTake
        ? { orderBy: PunctDeLucruGroupByArgs['orderBy'] }
        : { orderBy?: PunctDeLucruGroupByArgs['orderBy'] },
      OrderFields extends ExcludeUnderscoreKeys<Keys<MaybeTupleToUnion<T['orderBy']>>>,
      ByFields extends MaybeTupleToUnion<T['by']>,
      ByValid extends Has<ByFields, OrderFields>,
      HavingFields extends GetHavingFields<T['having']>,
      HavingValid extends Has<ByFields, HavingFields>,
      ByEmpty extends T['by'] extends never[] ? True : False,
      InputErrors extends ByEmpty extends True
      ? `Error: "by" must not be empty.`
      : HavingValid extends False
      ? {
          [P in HavingFields]: P extends ByFields
            ? never
            : P extends string
            ? `Error: Field "${P}" used in "having" needs to be provided in "by".`
            : [
                Error,
                'Field ',
                P,
                ` in "having" needs to be provided in "by"`,
              ]
        }[HavingFields]
      : 'take' extends Keys<T>
      ? 'orderBy' extends Keys<T>
        ? ByValid extends True
          ? {}
          : {
              [P in OrderFields]: P extends ByFields
                ? never
                : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
            }[OrderFields]
        : 'Error: If you provide "take", you also need to provide "orderBy"'
      : 'skip' extends Keys<T>
      ? 'orderBy' extends Keys<T>
        ? ByValid extends True
          ? {}
          : {
              [P in OrderFields]: P extends ByFields
                ? never
                : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
            }[OrderFields]
        : 'Error: If you provide "skip", you also need to provide "orderBy"'
      : ByValid extends True
      ? {}
      : {
          [P in OrderFields]: P extends ByFields
            ? never
            : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
        }[OrderFields]
    >(args: SubsetIntersection<T, PunctDeLucruGroupByArgs, OrderByArg> & InputErrors): {} extends InputErrors ? GetPunctDeLucruGroupByPayload<T> : Prisma.PrismaPromise<InputErrors>
  /**
   * Fields of the PunctDeLucru model
   */
  readonly fields: PunctDeLucruFieldRefs;
  }

  /**
   * The delegate class that acts as a "Promise-like" for PunctDeLucru.
   * Why is this prefixed with `Prisma__`?
   * Because we want to prevent naming conflicts as mentioned in
   * https://github.com/prisma/prisma-client-js/issues/707
   */
  export interface Prisma__PunctDeLucruClient<T, Null = never, ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs, GlobalOmitOptions = {}> extends Prisma.PrismaPromise<T> {
    readonly [Symbol.toStringTag]: "PrismaPromise"
    client<T extends ClientDefaultArgs<ExtArgs> = {}>(args?: Subset<T, ClientDefaultArgs<ExtArgs>>): Prisma__ClientClient<$Result.GetResult<Prisma.$ClientPayload<ExtArgs>, T, "findUniqueOrThrow", GlobalOmitOptions> | Null, Null, ExtArgs, GlobalOmitOptions>
    /**
     * Attaches callbacks for the resolution and/or rejection of the Promise.
     * @param onfulfilled The callback to execute when the Promise is resolved.
     * @param onrejected The callback to execute when the Promise is rejected.
     * @returns A Promise for the completion of which ever callback is executed.
     */
    then<TResult1 = T, TResult2 = never>(onfulfilled?: ((value: T) => TResult1 | PromiseLike<TResult1>) | undefined | null, onrejected?: ((reason: any) => TResult2 | PromiseLike<TResult2>) | undefined | null): $Utils.JsPromise<TResult1 | TResult2>
    /**
     * Attaches a callback for only the rejection of the Promise.
     * @param onrejected The callback to execute when the Promise is rejected.
     * @returns A Promise for the completion of the callback.
     */
    catch<TResult = never>(onrejected?: ((reason: any) => TResult | PromiseLike<TResult>) | undefined | null): $Utils.JsPromise<T | TResult>
    /**
     * Attaches a callback that is invoked when the Promise is settled (fulfilled or rejected). The
     * resolved value cannot be modified from the callback.
     * @param onfinally The callback to execute when the Promise is settled (fulfilled or rejected).
     * @returns A Promise for the completion of the callback.
     */
    finally(onfinally?: (() => void) | undefined | null): $Utils.JsPromise<T>
  }




  /**
   * Fields of the PunctDeLucru model
   */
  interface PunctDeLucruFieldRefs {
    readonly id: FieldRef<"PunctDeLucru", 'Int'>
    readonly denumire: FieldRef<"PunctDeLucru", 'String'>
    readonly deLa: FieldRef<"PunctDeLucru", 'DateTime'>
    readonly panaLa: FieldRef<"PunctDeLucru", 'DateTime'>
    readonly administratie: FieldRef<"PunctDeLucru", 'Administratie'>
    readonly registruUC: FieldRef<"PunctDeLucru", 'Boolean'>
    readonly salariati: FieldRef<"PunctDeLucru", 'Int'>
    readonly cui: FieldRef<"PunctDeLucru", 'String'>
    readonly casaDeMarcat: FieldRef<"PunctDeLucru", 'Boolean'>
    readonly clientId: FieldRef<"PunctDeLucru", 'Int'>
  }
    

  // Custom InputTypes
  /**
   * PunctDeLucru findUnique
   */
  export type PunctDeLucruFindUniqueArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the PunctDeLucru
     */
    select?: PunctDeLucruSelect<ExtArgs> | null
    /**
     * Omit specific fields from the PunctDeLucru
     */
    omit?: PunctDeLucruOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: PunctDeLucruInclude<ExtArgs> | null
    /**
     * Filter, which PunctDeLucru to fetch.
     */
    where: PunctDeLucruWhereUniqueInput
  }

  /**
   * PunctDeLucru findUniqueOrThrow
   */
  export type PunctDeLucruFindUniqueOrThrowArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the PunctDeLucru
     */
    select?: PunctDeLucruSelect<ExtArgs> | null
    /**
     * Omit specific fields from the PunctDeLucru
     */
    omit?: PunctDeLucruOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: PunctDeLucruInclude<ExtArgs> | null
    /**
     * Filter, which PunctDeLucru to fetch.
     */
    where: PunctDeLucruWhereUniqueInput
  }

  /**
   * PunctDeLucru findFirst
   */
  export type PunctDeLucruFindFirstArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the PunctDeLucru
     */
    select?: PunctDeLucruSelect<ExtArgs> | null
    /**
     * Omit specific fields from the PunctDeLucru
     */
    omit?: PunctDeLucruOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: PunctDeLucruInclude<ExtArgs> | null
    /**
     * Filter, which PunctDeLucru to fetch.
     */
    where?: PunctDeLucruWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of PunctDeLucrus to fetch.
     */
    orderBy?: PunctDeLucruOrderByWithRelationInput | PunctDeLucruOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for searching for PunctDeLucrus.
     */
    cursor?: PunctDeLucruWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` PunctDeLucrus from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` PunctDeLucrus.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/distinct Distinct Docs}
     * 
     * Filter by unique combinations of PunctDeLucrus.
     */
    distinct?: PunctDeLucruScalarFieldEnum | PunctDeLucruScalarFieldEnum[]
  }

  /**
   * PunctDeLucru findFirstOrThrow
   */
  export type PunctDeLucruFindFirstOrThrowArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the PunctDeLucru
     */
    select?: PunctDeLucruSelect<ExtArgs> | null
    /**
     * Omit specific fields from the PunctDeLucru
     */
    omit?: PunctDeLucruOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: PunctDeLucruInclude<ExtArgs> | null
    /**
     * Filter, which PunctDeLucru to fetch.
     */
    where?: PunctDeLucruWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of PunctDeLucrus to fetch.
     */
    orderBy?: PunctDeLucruOrderByWithRelationInput | PunctDeLucruOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for searching for PunctDeLucrus.
     */
    cursor?: PunctDeLucruWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` PunctDeLucrus from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` PunctDeLucrus.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/distinct Distinct Docs}
     * 
     * Filter by unique combinations of PunctDeLucrus.
     */
    distinct?: PunctDeLucruScalarFieldEnum | PunctDeLucruScalarFieldEnum[]
  }

  /**
   * PunctDeLucru findMany
   */
  export type PunctDeLucruFindManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the PunctDeLucru
     */
    select?: PunctDeLucruSelect<ExtArgs> | null
    /**
     * Omit specific fields from the PunctDeLucru
     */
    omit?: PunctDeLucruOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: PunctDeLucruInclude<ExtArgs> | null
    /**
     * Filter, which PunctDeLucrus to fetch.
     */
    where?: PunctDeLucruWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of PunctDeLucrus to fetch.
     */
    orderBy?: PunctDeLucruOrderByWithRelationInput | PunctDeLucruOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for listing PunctDeLucrus.
     */
    cursor?: PunctDeLucruWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` PunctDeLucrus from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` PunctDeLucrus.
     */
    skip?: number
    distinct?: PunctDeLucruScalarFieldEnum | PunctDeLucruScalarFieldEnum[]
  }

  /**
   * PunctDeLucru create
   */
  export type PunctDeLucruCreateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the PunctDeLucru
     */
    select?: PunctDeLucruSelect<ExtArgs> | null
    /**
     * Omit specific fields from the PunctDeLucru
     */
    omit?: PunctDeLucruOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: PunctDeLucruInclude<ExtArgs> | null
    /**
     * The data needed to create a PunctDeLucru.
     */
    data: XOR<PunctDeLucruCreateInput, PunctDeLucruUncheckedCreateInput>
  }

  /**
   * PunctDeLucru createMany
   */
  export type PunctDeLucruCreateManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * The data used to create many PunctDeLucrus.
     */
    data: PunctDeLucruCreateManyInput | PunctDeLucruCreateManyInput[]
    skipDuplicates?: boolean
  }

  /**
   * PunctDeLucru createManyAndReturn
   */
  export type PunctDeLucruCreateManyAndReturnArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the PunctDeLucru
     */
    select?: PunctDeLucruSelectCreateManyAndReturn<ExtArgs> | null
    /**
     * Omit specific fields from the PunctDeLucru
     */
    omit?: PunctDeLucruOmit<ExtArgs> | null
    /**
     * The data used to create many PunctDeLucrus.
     */
    data: PunctDeLucruCreateManyInput | PunctDeLucruCreateManyInput[]
    skipDuplicates?: boolean
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: PunctDeLucruIncludeCreateManyAndReturn<ExtArgs> | null
  }

  /**
   * PunctDeLucru update
   */
  export type PunctDeLucruUpdateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the PunctDeLucru
     */
    select?: PunctDeLucruSelect<ExtArgs> | null
    /**
     * Omit specific fields from the PunctDeLucru
     */
    omit?: PunctDeLucruOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: PunctDeLucruInclude<ExtArgs> | null
    /**
     * The data needed to update a PunctDeLucru.
     */
    data: XOR<PunctDeLucruUpdateInput, PunctDeLucruUncheckedUpdateInput>
    /**
     * Choose, which PunctDeLucru to update.
     */
    where: PunctDeLucruWhereUniqueInput
  }

  /**
   * PunctDeLucru updateMany
   */
  export type PunctDeLucruUpdateManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * The data used to update PunctDeLucrus.
     */
    data: XOR<PunctDeLucruUpdateManyMutationInput, PunctDeLucruUncheckedUpdateManyInput>
    /**
     * Filter which PunctDeLucrus to update
     */
    where?: PunctDeLucruWhereInput
    /**
     * Limit how many PunctDeLucrus to update.
     */
    limit?: number
  }

  /**
   * PunctDeLucru updateManyAndReturn
   */
  export type PunctDeLucruUpdateManyAndReturnArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the PunctDeLucru
     */
    select?: PunctDeLucruSelectUpdateManyAndReturn<ExtArgs> | null
    /**
     * Omit specific fields from the PunctDeLucru
     */
    omit?: PunctDeLucruOmit<ExtArgs> | null
    /**
     * The data used to update PunctDeLucrus.
     */
    data: XOR<PunctDeLucruUpdateManyMutationInput, PunctDeLucruUncheckedUpdateManyInput>
    /**
     * Filter which PunctDeLucrus to update
     */
    where?: PunctDeLucruWhereInput
    /**
     * Limit how many PunctDeLucrus to update.
     */
    limit?: number
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: PunctDeLucruIncludeUpdateManyAndReturn<ExtArgs> | null
  }

  /**
   * PunctDeLucru upsert
   */
  export type PunctDeLucruUpsertArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the PunctDeLucru
     */
    select?: PunctDeLucruSelect<ExtArgs> | null
    /**
     * Omit specific fields from the PunctDeLucru
     */
    omit?: PunctDeLucruOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: PunctDeLucruInclude<ExtArgs> | null
    /**
     * The filter to search for the PunctDeLucru to update in case it exists.
     */
    where: PunctDeLucruWhereUniqueInput
    /**
     * In case the PunctDeLucru found by the `where` argument doesn't exist, create a new PunctDeLucru with this data.
     */
    create: XOR<PunctDeLucruCreateInput, PunctDeLucruUncheckedCreateInput>
    /**
     * In case the PunctDeLucru was found with the provided `where` argument, update it with this data.
     */
    update: XOR<PunctDeLucruUpdateInput, PunctDeLucruUncheckedUpdateInput>
  }

  /**
   * PunctDeLucru delete
   */
  export type PunctDeLucruDeleteArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the PunctDeLucru
     */
    select?: PunctDeLucruSelect<ExtArgs> | null
    /**
     * Omit specific fields from the PunctDeLucru
     */
    omit?: PunctDeLucruOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: PunctDeLucruInclude<ExtArgs> | null
    /**
     * Filter which PunctDeLucru to delete.
     */
    where: PunctDeLucruWhereUniqueInput
  }

  /**
   * PunctDeLucru deleteMany
   */
  export type PunctDeLucruDeleteManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Filter which PunctDeLucrus to delete
     */
    where?: PunctDeLucruWhereInput
    /**
     * Limit how many PunctDeLucrus to delete.
     */
    limit?: number
  }

  /**
   * PunctDeLucru without action
   */
  export type PunctDeLucruDefaultArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the PunctDeLucru
     */
    select?: PunctDeLucruSelect<ExtArgs> | null
    /**
     * Omit specific fields from the PunctDeLucru
     */
    omit?: PunctDeLucruOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: PunctDeLucruInclude<ExtArgs> | null
  }


  /**
   * Model Istoric
   */

  export type AggregateIstoric = {
    _count: IstoricCountAggregateOutputType | null
    _avg: IstoricAvgAggregateOutputType | null
    _sum: IstoricSumAggregateOutputType | null
    _min: IstoricMinAggregateOutputType | null
    _max: IstoricMaxAggregateOutputType | null
  }

  export type IstoricAvgAggregateOutputType = {
    id: number | null
    anul: number | null
    cifraAfaceri: number | null
    clientId: number | null
  }

  export type IstoricSumAggregateOutputType = {
    id: number | null
    anul: number | null
    cifraAfaceri: number | null
    clientId: number | null
  }

  export type IstoricMinAggregateOutputType = {
    id: number | null
    anul: number | null
    cifraAfaceri: number | null
    inventar: boolean | null
    bilantSemIun: $Enums.DaNuNuECazul | null
    bilantAnual: $Enums.DaNuNuECazul | null
    clientId: number | null
  }

  export type IstoricMaxAggregateOutputType = {
    id: number | null
    anul: number | null
    cifraAfaceri: number | null
    inventar: boolean | null
    bilantSemIun: $Enums.DaNuNuECazul | null
    bilantAnual: $Enums.DaNuNuECazul | null
    clientId: number | null
  }

  export type IstoricCountAggregateOutputType = {
    id: number
    anul: number
    cifraAfaceri: number
    inventar: number
    bilantSemIun: number
    bilantAnual: number
    clientId: number
    _all: number
  }


  export type IstoricAvgAggregateInputType = {
    id?: true
    anul?: true
    cifraAfaceri?: true
    clientId?: true
  }

  export type IstoricSumAggregateInputType = {
    id?: true
    anul?: true
    cifraAfaceri?: true
    clientId?: true
  }

  export type IstoricMinAggregateInputType = {
    id?: true
    anul?: true
    cifraAfaceri?: true
    inventar?: true
    bilantSemIun?: true
    bilantAnual?: true
    clientId?: true
  }

  export type IstoricMaxAggregateInputType = {
    id?: true
    anul?: true
    cifraAfaceri?: true
    inventar?: true
    bilantSemIun?: true
    bilantAnual?: true
    clientId?: true
  }

  export type IstoricCountAggregateInputType = {
    id?: true
    anul?: true
    cifraAfaceri?: true
    inventar?: true
    bilantSemIun?: true
    bilantAnual?: true
    clientId?: true
    _all?: true
  }

  export type IstoricAggregateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Filter which Istoric to aggregate.
     */
    where?: IstoricWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Istorics to fetch.
     */
    orderBy?: IstoricOrderByWithRelationInput | IstoricOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the start position
     */
    cursor?: IstoricWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Istorics from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Istorics.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Count returned Istorics
    **/
    _count?: true | IstoricCountAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to average
    **/
    _avg?: IstoricAvgAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to sum
    **/
    _sum?: IstoricSumAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to find the minimum value
    **/
    _min?: IstoricMinAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to find the maximum value
    **/
    _max?: IstoricMaxAggregateInputType
  }

  export type GetIstoricAggregateType<T extends IstoricAggregateArgs> = {
        [P in keyof T & keyof AggregateIstoric]: P extends '_count' | 'count'
      ? T[P] extends true
        ? number
        : GetScalarType<T[P], AggregateIstoric[P]>
      : GetScalarType<T[P], AggregateIstoric[P]>
  }




  export type IstoricGroupByArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    where?: IstoricWhereInput
    orderBy?: IstoricOrderByWithAggregationInput | IstoricOrderByWithAggregationInput[]
    by: IstoricScalarFieldEnum[] | IstoricScalarFieldEnum
    having?: IstoricScalarWhereWithAggregatesInput
    take?: number
    skip?: number
    _count?: IstoricCountAggregateInputType | true
    _avg?: IstoricAvgAggregateInputType
    _sum?: IstoricSumAggregateInputType
    _min?: IstoricMinAggregateInputType
    _max?: IstoricMaxAggregateInputType
  }

  export type IstoricGroupByOutputType = {
    id: number
    anul: number
    cifraAfaceri: number
    inventar: boolean
    bilantSemIun: $Enums.DaNuNuECazul
    bilantAnual: $Enums.DaNuNuECazul
    clientId: number
    _count: IstoricCountAggregateOutputType | null
    _avg: IstoricAvgAggregateOutputType | null
    _sum: IstoricSumAggregateOutputType | null
    _min: IstoricMinAggregateOutputType | null
    _max: IstoricMaxAggregateOutputType | null
  }

  type GetIstoricGroupByPayload<T extends IstoricGroupByArgs> = Prisma.PrismaPromise<
    Array<
      PickEnumerable<IstoricGroupByOutputType, T['by']> &
        {
          [P in ((keyof T) & (keyof IstoricGroupByOutputType))]: P extends '_count'
            ? T[P] extends boolean
              ? number
              : GetScalarType<T[P], IstoricGroupByOutputType[P]>
            : GetScalarType<T[P], IstoricGroupByOutputType[P]>
        }
      >
    >


  export type IstoricSelect<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    anul?: boolean
    cifraAfaceri?: boolean
    inventar?: boolean
    bilantSemIun?: boolean
    bilantAnual?: boolean
    clientId?: boolean
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["istoric"]>

  export type IstoricSelectCreateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    anul?: boolean
    cifraAfaceri?: boolean
    inventar?: boolean
    bilantSemIun?: boolean
    bilantAnual?: boolean
    clientId?: boolean
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["istoric"]>

  export type IstoricSelectUpdateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    anul?: boolean
    cifraAfaceri?: boolean
    inventar?: boolean
    bilantSemIun?: boolean
    bilantAnual?: boolean
    clientId?: boolean
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["istoric"]>

  export type IstoricSelectScalar = {
    id?: boolean
    anul?: boolean
    cifraAfaceri?: boolean
    inventar?: boolean
    bilantSemIun?: boolean
    bilantAnual?: boolean
    clientId?: boolean
  }

  export type IstoricOmit<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetOmit<"id" | "anul" | "cifraAfaceri" | "inventar" | "bilantSemIun" | "bilantAnual" | "clientId", ExtArgs["result"]["istoric"]>
  export type IstoricInclude<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }
  export type IstoricIncludeCreateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }
  export type IstoricIncludeUpdateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }

  export type $IstoricPayload<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    name: "Istoric"
    objects: {
      client: Prisma.$ClientPayload<ExtArgs>
    }
    scalars: $Extensions.GetPayloadResult<{
      id: number
      anul: number
      cifraAfaceri: number
      inventar: boolean
      bilantSemIun: $Enums.DaNuNuECazul
      bilantAnual: $Enums.DaNuNuECazul
      clientId: number
    }, ExtArgs["result"]["istoric"]>
    composites: {}
  }

  type IstoricGetPayload<S extends boolean | null | undefined | IstoricDefaultArgs> = $Result.GetResult<Prisma.$IstoricPayload, S>

  type IstoricCountArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> =
    Omit<IstoricFindManyArgs, 'select' | 'include' | 'distinct' | 'omit'> & {
      select?: IstoricCountAggregateInputType | true
    }

  export interface IstoricDelegate<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs, GlobalOmitOptions = {}> {
    [K: symbol]: { types: Prisma.TypeMap<ExtArgs>['model']['Istoric'], meta: { name: 'Istoric' } }
    /**
     * Find zero or one Istoric that matches the filter.
     * @param {IstoricFindUniqueArgs} args - Arguments to find a Istoric
     * @example
     * // Get one Istoric
     * const istoric = await prisma.istoric.findUnique({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findUnique<T extends IstoricFindUniqueArgs>(args: SelectSubset<T, IstoricFindUniqueArgs<ExtArgs>>): Prisma__IstoricClient<$Result.GetResult<Prisma.$IstoricPayload<ExtArgs>, T, "findUnique", GlobalOmitOptions> | null, null, ExtArgs, GlobalOmitOptions>

    /**
     * Find one Istoric that matches the filter or throw an error with `error.code='P2025'`
     * if no matches were found.
     * @param {IstoricFindUniqueOrThrowArgs} args - Arguments to find a Istoric
     * @example
     * // Get one Istoric
     * const istoric = await prisma.istoric.findUniqueOrThrow({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findUniqueOrThrow<T extends IstoricFindUniqueOrThrowArgs>(args: SelectSubset<T, IstoricFindUniqueOrThrowArgs<ExtArgs>>): Prisma__IstoricClient<$Result.GetResult<Prisma.$IstoricPayload<ExtArgs>, T, "findUniqueOrThrow", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Find the first Istoric that matches the filter.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {IstoricFindFirstArgs} args - Arguments to find a Istoric
     * @example
     * // Get one Istoric
     * const istoric = await prisma.istoric.findFirst({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findFirst<T extends IstoricFindFirstArgs>(args?: SelectSubset<T, IstoricFindFirstArgs<ExtArgs>>): Prisma__IstoricClient<$Result.GetResult<Prisma.$IstoricPayload<ExtArgs>, T, "findFirst", GlobalOmitOptions> | null, null, ExtArgs, GlobalOmitOptions>

    /**
     * Find the first Istoric that matches the filter or
     * throw `PrismaKnownClientError` with `P2025` code if no matches were found.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {IstoricFindFirstOrThrowArgs} args - Arguments to find a Istoric
     * @example
     * // Get one Istoric
     * const istoric = await prisma.istoric.findFirstOrThrow({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findFirstOrThrow<T extends IstoricFindFirstOrThrowArgs>(args?: SelectSubset<T, IstoricFindFirstOrThrowArgs<ExtArgs>>): Prisma__IstoricClient<$Result.GetResult<Prisma.$IstoricPayload<ExtArgs>, T, "findFirstOrThrow", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Find zero or more Istorics that matches the filter.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {IstoricFindManyArgs} args - Arguments to filter and select certain fields only.
     * @example
     * // Get all Istorics
     * const istorics = await prisma.istoric.findMany()
     * 
     * // Get first 10 Istorics
     * const istorics = await prisma.istoric.findMany({ take: 10 })
     * 
     * // Only select the `id`
     * const istoricWithIdOnly = await prisma.istoric.findMany({ select: { id: true } })
     * 
     */
    findMany<T extends IstoricFindManyArgs>(args?: SelectSubset<T, IstoricFindManyArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$IstoricPayload<ExtArgs>, T, "findMany", GlobalOmitOptions>>

    /**
     * Create a Istoric.
     * @param {IstoricCreateArgs} args - Arguments to create a Istoric.
     * @example
     * // Create one Istoric
     * const Istoric = await prisma.istoric.create({
     *   data: {
     *     // ... data to create a Istoric
     *   }
     * })
     * 
     */
    create<T extends IstoricCreateArgs>(args: SelectSubset<T, IstoricCreateArgs<ExtArgs>>): Prisma__IstoricClient<$Result.GetResult<Prisma.$IstoricPayload<ExtArgs>, T, "create", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Create many Istorics.
     * @param {IstoricCreateManyArgs} args - Arguments to create many Istorics.
     * @example
     * // Create many Istorics
     * const istoric = await prisma.istoric.createMany({
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     *     
     */
    createMany<T extends IstoricCreateManyArgs>(args?: SelectSubset<T, IstoricCreateManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Create many Istorics and returns the data saved in the database.
     * @param {IstoricCreateManyAndReturnArgs} args - Arguments to create many Istorics.
     * @example
     * // Create many Istorics
     * const istoric = await prisma.istoric.createManyAndReturn({
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * 
     * // Create many Istorics and only return the `id`
     * const istoricWithIdOnly = await prisma.istoric.createManyAndReturn({
     *   select: { id: true },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * 
     */
    createManyAndReturn<T extends IstoricCreateManyAndReturnArgs>(args?: SelectSubset<T, IstoricCreateManyAndReturnArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$IstoricPayload<ExtArgs>, T, "createManyAndReturn", GlobalOmitOptions>>

    /**
     * Delete a Istoric.
     * @param {IstoricDeleteArgs} args - Arguments to delete one Istoric.
     * @example
     * // Delete one Istoric
     * const Istoric = await prisma.istoric.delete({
     *   where: {
     *     // ... filter to delete one Istoric
     *   }
     * })
     * 
     */
    delete<T extends IstoricDeleteArgs>(args: SelectSubset<T, IstoricDeleteArgs<ExtArgs>>): Prisma__IstoricClient<$Result.GetResult<Prisma.$IstoricPayload<ExtArgs>, T, "delete", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Update one Istoric.
     * @param {IstoricUpdateArgs} args - Arguments to update one Istoric.
     * @example
     * // Update one Istoric
     * const istoric = await prisma.istoric.update({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: {
     *     // ... provide data here
     *   }
     * })
     * 
     */
    update<T extends IstoricUpdateArgs>(args: SelectSubset<T, IstoricUpdateArgs<ExtArgs>>): Prisma__IstoricClient<$Result.GetResult<Prisma.$IstoricPayload<ExtArgs>, T, "update", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Delete zero or more Istorics.
     * @param {IstoricDeleteManyArgs} args - Arguments to filter Istorics to delete.
     * @example
     * // Delete a few Istorics
     * const { count } = await prisma.istoric.deleteMany({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     * 
     */
    deleteMany<T extends IstoricDeleteManyArgs>(args?: SelectSubset<T, IstoricDeleteManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Update zero or more Istorics.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {IstoricUpdateManyArgs} args - Arguments to update one or more rows.
     * @example
     * // Update many Istorics
     * const istoric = await prisma.istoric.updateMany({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: {
     *     // ... provide data here
     *   }
     * })
     * 
     */
    updateMany<T extends IstoricUpdateManyArgs>(args: SelectSubset<T, IstoricUpdateManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Update zero or more Istorics and returns the data updated in the database.
     * @param {IstoricUpdateManyAndReturnArgs} args - Arguments to update many Istorics.
     * @example
     * // Update many Istorics
     * const istoric = await prisma.istoric.updateManyAndReturn({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * 
     * // Update zero or more Istorics and only return the `id`
     * const istoricWithIdOnly = await prisma.istoric.updateManyAndReturn({
     *   select: { id: true },
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * 
     */
    updateManyAndReturn<T extends IstoricUpdateManyAndReturnArgs>(args: SelectSubset<T, IstoricUpdateManyAndReturnArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$IstoricPayload<ExtArgs>, T, "updateManyAndReturn", GlobalOmitOptions>>

    /**
     * Create or update one Istoric.
     * @param {IstoricUpsertArgs} args - Arguments to update or create a Istoric.
     * @example
     * // Update or create a Istoric
     * const istoric = await prisma.istoric.upsert({
     *   create: {
     *     // ... data to create a Istoric
     *   },
     *   update: {
     *     // ... in case it already exists, update
     *   },
     *   where: {
     *     // ... the filter for the Istoric we want to update
     *   }
     * })
     */
    upsert<T extends IstoricUpsertArgs>(args: SelectSubset<T, IstoricUpsertArgs<ExtArgs>>): Prisma__IstoricClient<$Result.GetResult<Prisma.$IstoricPayload<ExtArgs>, T, "upsert", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>


    /**
     * Count the number of Istorics.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {IstoricCountArgs} args - Arguments to filter Istorics to count.
     * @example
     * // Count the number of Istorics
     * const count = await prisma.istoric.count({
     *   where: {
     *     // ... the filter for the Istorics we want to count
     *   }
     * })
    **/
    count<T extends IstoricCountArgs>(
      args?: Subset<T, IstoricCountArgs>,
    ): Prisma.PrismaPromise<
      T extends $Utils.Record<'select', any>
        ? T['select'] extends true
          ? number
          : GetScalarType<T['select'], IstoricCountAggregateOutputType>
        : number
    >

    /**
     * Allows you to perform aggregations operations on a Istoric.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {IstoricAggregateArgs} args - Select which aggregations you would like to apply and on what fields.
     * @example
     * // Ordered by age ascending
     * // Where email contains prisma.io
     * // Limited to the 10 users
     * const aggregations = await prisma.user.aggregate({
     *   _avg: {
     *     age: true,
     *   },
     *   where: {
     *     email: {
     *       contains: "prisma.io",
     *     },
     *   },
     *   orderBy: {
     *     age: "asc",
     *   },
     *   take: 10,
     * })
    **/
    aggregate<T extends IstoricAggregateArgs>(args: Subset<T, IstoricAggregateArgs>): Prisma.PrismaPromise<GetIstoricAggregateType<T>>

    /**
     * Group by Istoric.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {IstoricGroupByArgs} args - Group by arguments.
     * @example
     * // Group by city, order by createdAt, get count
     * const result = await prisma.user.groupBy({
     *   by: ['city', 'createdAt'],
     *   orderBy: {
     *     createdAt: true
     *   },
     *   _count: {
     *     _all: true
     *   },
     * })
     * 
    **/
    groupBy<
      T extends IstoricGroupByArgs,
      HasSelectOrTake extends Or<
        Extends<'skip', Keys<T>>,
        Extends<'take', Keys<T>>
      >,
      OrderByArg extends True extends HasSelectOrTake
        ? { orderBy: IstoricGroupByArgs['orderBy'] }
        : { orderBy?: IstoricGroupByArgs['orderBy'] },
      OrderFields extends ExcludeUnderscoreKeys<Keys<MaybeTupleToUnion<T['orderBy']>>>,
      ByFields extends MaybeTupleToUnion<T['by']>,
      ByValid extends Has<ByFields, OrderFields>,
      HavingFields extends GetHavingFields<T['having']>,
      HavingValid extends Has<ByFields, HavingFields>,
      ByEmpty extends T['by'] extends never[] ? True : False,
      InputErrors extends ByEmpty extends True
      ? `Error: "by" must not be empty.`
      : HavingValid extends False
      ? {
          [P in HavingFields]: P extends ByFields
            ? never
            : P extends string
            ? `Error: Field "${P}" used in "having" needs to be provided in "by".`
            : [
                Error,
                'Field ',
                P,
                ` in "having" needs to be provided in "by"`,
              ]
        }[HavingFields]
      : 'take' extends Keys<T>
      ? 'orderBy' extends Keys<T>
        ? ByValid extends True
          ? {}
          : {
              [P in OrderFields]: P extends ByFields
                ? never
                : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
            }[OrderFields]
        : 'Error: If you provide "take", you also need to provide "orderBy"'
      : 'skip' extends Keys<T>
      ? 'orderBy' extends Keys<T>
        ? ByValid extends True
          ? {}
          : {
              [P in OrderFields]: P extends ByFields
                ? never
                : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
            }[OrderFields]
        : 'Error: If you provide "skip", you also need to provide "orderBy"'
      : ByValid extends True
      ? {}
      : {
          [P in OrderFields]: P extends ByFields
            ? never
            : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
        }[OrderFields]
    >(args: SubsetIntersection<T, IstoricGroupByArgs, OrderByArg> & InputErrors): {} extends InputErrors ? GetIstoricGroupByPayload<T> : Prisma.PrismaPromise<InputErrors>
  /**
   * Fields of the Istoric model
   */
  readonly fields: IstoricFieldRefs;
  }

  /**
   * The delegate class that acts as a "Promise-like" for Istoric.
   * Why is this prefixed with `Prisma__`?
   * Because we want to prevent naming conflicts as mentioned in
   * https://github.com/prisma/prisma-client-js/issues/707
   */
  export interface Prisma__IstoricClient<T, Null = never, ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs, GlobalOmitOptions = {}> extends Prisma.PrismaPromise<T> {
    readonly [Symbol.toStringTag]: "PrismaPromise"
    client<T extends ClientDefaultArgs<ExtArgs> = {}>(args?: Subset<T, ClientDefaultArgs<ExtArgs>>): Prisma__ClientClient<$Result.GetResult<Prisma.$ClientPayload<ExtArgs>, T, "findUniqueOrThrow", GlobalOmitOptions> | Null, Null, ExtArgs, GlobalOmitOptions>
    /**
     * Attaches callbacks for the resolution and/or rejection of the Promise.
     * @param onfulfilled The callback to execute when the Promise is resolved.
     * @param onrejected The callback to execute when the Promise is rejected.
     * @returns A Promise for the completion of which ever callback is executed.
     */
    then<TResult1 = T, TResult2 = never>(onfulfilled?: ((value: T) => TResult1 | PromiseLike<TResult1>) | undefined | null, onrejected?: ((reason: any) => TResult2 | PromiseLike<TResult2>) | undefined | null): $Utils.JsPromise<TResult1 | TResult2>
    /**
     * Attaches a callback for only the rejection of the Promise.
     * @param onrejected The callback to execute when the Promise is rejected.
     * @returns A Promise for the completion of the callback.
     */
    catch<TResult = never>(onrejected?: ((reason: any) => TResult | PromiseLike<TResult>) | undefined | null): $Utils.JsPromise<T | TResult>
    /**
     * Attaches a callback that is invoked when the Promise is settled (fulfilled or rejected). The
     * resolved value cannot be modified from the callback.
     * @param onfinally The callback to execute when the Promise is settled (fulfilled or rejected).
     * @returns A Promise for the completion of the callback.
     */
    finally(onfinally?: (() => void) | undefined | null): $Utils.JsPromise<T>
  }




  /**
   * Fields of the Istoric model
   */
  interface IstoricFieldRefs {
    readonly id: FieldRef<"Istoric", 'Int'>
    readonly anul: FieldRef<"Istoric", 'Int'>
    readonly cifraAfaceri: FieldRef<"Istoric", 'Float'>
    readonly inventar: FieldRef<"Istoric", 'Boolean'>
    readonly bilantSemIun: FieldRef<"Istoric", 'DaNuNuECazul'>
    readonly bilantAnual: FieldRef<"Istoric", 'DaNuNuECazul'>
    readonly clientId: FieldRef<"Istoric", 'Int'>
  }
    

  // Custom InputTypes
  /**
   * Istoric findUnique
   */
  export type IstoricFindUniqueArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Istoric
     */
    select?: IstoricSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Istoric
     */
    omit?: IstoricOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: IstoricInclude<ExtArgs> | null
    /**
     * Filter, which Istoric to fetch.
     */
    where: IstoricWhereUniqueInput
  }

  /**
   * Istoric findUniqueOrThrow
   */
  export type IstoricFindUniqueOrThrowArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Istoric
     */
    select?: IstoricSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Istoric
     */
    omit?: IstoricOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: IstoricInclude<ExtArgs> | null
    /**
     * Filter, which Istoric to fetch.
     */
    where: IstoricWhereUniqueInput
  }

  /**
   * Istoric findFirst
   */
  export type IstoricFindFirstArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Istoric
     */
    select?: IstoricSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Istoric
     */
    omit?: IstoricOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: IstoricInclude<ExtArgs> | null
    /**
     * Filter, which Istoric to fetch.
     */
    where?: IstoricWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Istorics to fetch.
     */
    orderBy?: IstoricOrderByWithRelationInput | IstoricOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for searching for Istorics.
     */
    cursor?: IstoricWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Istorics from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Istorics.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/distinct Distinct Docs}
     * 
     * Filter by unique combinations of Istorics.
     */
    distinct?: IstoricScalarFieldEnum | IstoricScalarFieldEnum[]
  }

  /**
   * Istoric findFirstOrThrow
   */
  export type IstoricFindFirstOrThrowArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Istoric
     */
    select?: IstoricSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Istoric
     */
    omit?: IstoricOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: IstoricInclude<ExtArgs> | null
    /**
     * Filter, which Istoric to fetch.
     */
    where?: IstoricWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Istorics to fetch.
     */
    orderBy?: IstoricOrderByWithRelationInput | IstoricOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for searching for Istorics.
     */
    cursor?: IstoricWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Istorics from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Istorics.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/distinct Distinct Docs}
     * 
     * Filter by unique combinations of Istorics.
     */
    distinct?: IstoricScalarFieldEnum | IstoricScalarFieldEnum[]
  }

  /**
   * Istoric findMany
   */
  export type IstoricFindManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Istoric
     */
    select?: IstoricSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Istoric
     */
    omit?: IstoricOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: IstoricInclude<ExtArgs> | null
    /**
     * Filter, which Istorics to fetch.
     */
    where?: IstoricWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Istorics to fetch.
     */
    orderBy?: IstoricOrderByWithRelationInput | IstoricOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for listing Istorics.
     */
    cursor?: IstoricWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Istorics from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Istorics.
     */
    skip?: number
    distinct?: IstoricScalarFieldEnum | IstoricScalarFieldEnum[]
  }

  /**
   * Istoric create
   */
  export type IstoricCreateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Istoric
     */
    select?: IstoricSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Istoric
     */
    omit?: IstoricOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: IstoricInclude<ExtArgs> | null
    /**
     * The data needed to create a Istoric.
     */
    data: XOR<IstoricCreateInput, IstoricUncheckedCreateInput>
  }

  /**
   * Istoric createMany
   */
  export type IstoricCreateManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * The data used to create many Istorics.
     */
    data: IstoricCreateManyInput | IstoricCreateManyInput[]
    skipDuplicates?: boolean
  }

  /**
   * Istoric createManyAndReturn
   */
  export type IstoricCreateManyAndReturnArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Istoric
     */
    select?: IstoricSelectCreateManyAndReturn<ExtArgs> | null
    /**
     * Omit specific fields from the Istoric
     */
    omit?: IstoricOmit<ExtArgs> | null
    /**
     * The data used to create many Istorics.
     */
    data: IstoricCreateManyInput | IstoricCreateManyInput[]
    skipDuplicates?: boolean
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: IstoricIncludeCreateManyAndReturn<ExtArgs> | null
  }

  /**
   * Istoric update
   */
  export type IstoricUpdateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Istoric
     */
    select?: IstoricSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Istoric
     */
    omit?: IstoricOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: IstoricInclude<ExtArgs> | null
    /**
     * The data needed to update a Istoric.
     */
    data: XOR<IstoricUpdateInput, IstoricUncheckedUpdateInput>
    /**
     * Choose, which Istoric to update.
     */
    where: IstoricWhereUniqueInput
  }

  /**
   * Istoric updateMany
   */
  export type IstoricUpdateManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * The data used to update Istorics.
     */
    data: XOR<IstoricUpdateManyMutationInput, IstoricUncheckedUpdateManyInput>
    /**
     * Filter which Istorics to update
     */
    where?: IstoricWhereInput
    /**
     * Limit how many Istorics to update.
     */
    limit?: number
  }

  /**
   * Istoric updateManyAndReturn
   */
  export type IstoricUpdateManyAndReturnArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Istoric
     */
    select?: IstoricSelectUpdateManyAndReturn<ExtArgs> | null
    /**
     * Omit specific fields from the Istoric
     */
    omit?: IstoricOmit<ExtArgs> | null
    /**
     * The data used to update Istorics.
     */
    data: XOR<IstoricUpdateManyMutationInput, IstoricUncheckedUpdateManyInput>
    /**
     * Filter which Istorics to update
     */
    where?: IstoricWhereInput
    /**
     * Limit how many Istorics to update.
     */
    limit?: number
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: IstoricIncludeUpdateManyAndReturn<ExtArgs> | null
  }

  /**
   * Istoric upsert
   */
  export type IstoricUpsertArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Istoric
     */
    select?: IstoricSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Istoric
     */
    omit?: IstoricOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: IstoricInclude<ExtArgs> | null
    /**
     * The filter to search for the Istoric to update in case it exists.
     */
    where: IstoricWhereUniqueInput
    /**
     * In case the Istoric found by the `where` argument doesn't exist, create a new Istoric with this data.
     */
    create: XOR<IstoricCreateInput, IstoricUncheckedCreateInput>
    /**
     * In case the Istoric was found with the provided `where` argument, update it with this data.
     */
    update: XOR<IstoricUpdateInput, IstoricUncheckedUpdateInput>
  }

  /**
   * Istoric delete
   */
  export type IstoricDeleteArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Istoric
     */
    select?: IstoricSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Istoric
     */
    omit?: IstoricOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: IstoricInclude<ExtArgs> | null
    /**
     * Filter which Istoric to delete.
     */
    where: IstoricWhereUniqueInput
  }

  /**
   * Istoric deleteMany
   */
  export type IstoricDeleteManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Filter which Istorics to delete
     */
    where?: IstoricWhereInput
    /**
     * Limit how many Istorics to delete.
     */
    limit?: number
  }

  /**
   * Istoric without action
   */
  export type IstoricDefaultArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Istoric
     */
    select?: IstoricSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Istoric
     */
    omit?: IstoricOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: IstoricInclude<ExtArgs> | null
  }


  /**
   * Model Task
   */

  export type AggregateTask = {
    _count: TaskCountAggregateOutputType | null
    _avg: TaskAvgAggregateOutputType | null
    _sum: TaskSumAggregateOutputType | null
    _min: TaskMinAggregateOutputType | null
    _max: TaskMaxAggregateOutputType | null
  }

  export type TaskAvgAggregateOutputType = {
    id: number | null
    userId: number | null
    clientId: number | null
  }

  export type TaskSumAggregateOutputType = {
    id: number | null
    userId: number | null
    clientId: number | null
  }

  export type TaskMinAggregateOutputType = {
    id: number | null
    done: boolean | null
    title: string | null
    notes: string | null
    blocked: string | null
    objective: string | null
    date: Date | null
    userId: number | null
    clientId: number | null
  }

  export type TaskMaxAggregateOutputType = {
    id: number | null
    done: boolean | null
    title: string | null
    notes: string | null
    blocked: string | null
    objective: string | null
    date: Date | null
    userId: number | null
    clientId: number | null
  }

  export type TaskCountAggregateOutputType = {
    id: number
    done: number
    title: number
    notes: number
    blocked: number
    objective: number
    date: number
    userId: number
    clientId: number
    _all: number
  }


  export type TaskAvgAggregateInputType = {
    id?: true
    userId?: true
    clientId?: true
  }

  export type TaskSumAggregateInputType = {
    id?: true
    userId?: true
    clientId?: true
  }

  export type TaskMinAggregateInputType = {
    id?: true
    done?: true
    title?: true
    notes?: true
    blocked?: true
    objective?: true
    date?: true
    userId?: true
    clientId?: true
  }

  export type TaskMaxAggregateInputType = {
    id?: true
    done?: true
    title?: true
    notes?: true
    blocked?: true
    objective?: true
    date?: true
    userId?: true
    clientId?: true
  }

  export type TaskCountAggregateInputType = {
    id?: true
    done?: true
    title?: true
    notes?: true
    blocked?: true
    objective?: true
    date?: true
    userId?: true
    clientId?: true
    _all?: true
  }

  export type TaskAggregateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Filter which Task to aggregate.
     */
    where?: TaskWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Tasks to fetch.
     */
    orderBy?: TaskOrderByWithRelationInput | TaskOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the start position
     */
    cursor?: TaskWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Tasks from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Tasks.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Count returned Tasks
    **/
    _count?: true | TaskCountAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to average
    **/
    _avg?: TaskAvgAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to sum
    **/
    _sum?: TaskSumAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to find the minimum value
    **/
    _min?: TaskMinAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to find the maximum value
    **/
    _max?: TaskMaxAggregateInputType
  }

  export type GetTaskAggregateType<T extends TaskAggregateArgs> = {
        [P in keyof T & keyof AggregateTask]: P extends '_count' | 'count'
      ? T[P] extends true
        ? number
        : GetScalarType<T[P], AggregateTask[P]>
      : GetScalarType<T[P], AggregateTask[P]>
  }




  export type TaskGroupByArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    where?: TaskWhereInput
    orderBy?: TaskOrderByWithAggregationInput | TaskOrderByWithAggregationInput[]
    by: TaskScalarFieldEnum[] | TaskScalarFieldEnum
    having?: TaskScalarWhereWithAggregatesInput
    take?: number
    skip?: number
    _count?: TaskCountAggregateInputType | true
    _avg?: TaskAvgAggregateInputType
    _sum?: TaskSumAggregateInputType
    _min?: TaskMinAggregateInputType
    _max?: TaskMaxAggregateInputType
  }

  export type TaskGroupByOutputType = {
    id: number
    done: boolean
    title: string
    notes: string | null
    blocked: string | null
    objective: string | null
    date: Date
    userId: number
    clientId: number
    _count: TaskCountAggregateOutputType | null
    _avg: TaskAvgAggregateOutputType | null
    _sum: TaskSumAggregateOutputType | null
    _min: TaskMinAggregateOutputType | null
    _max: TaskMaxAggregateOutputType | null
  }

  type GetTaskGroupByPayload<T extends TaskGroupByArgs> = Prisma.PrismaPromise<
    Array<
      PickEnumerable<TaskGroupByOutputType, T['by']> &
        {
          [P in ((keyof T) & (keyof TaskGroupByOutputType))]: P extends '_count'
            ? T[P] extends boolean
              ? number
              : GetScalarType<T[P], TaskGroupByOutputType[P]>
            : GetScalarType<T[P], TaskGroupByOutputType[P]>
        }
      >
    >


  export type TaskSelect<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    done?: boolean
    title?: boolean
    notes?: boolean
    blocked?: boolean
    objective?: boolean
    date?: boolean
    userId?: boolean
    clientId?: boolean
    user?: boolean | UserDefaultArgs<ExtArgs>
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["task"]>

  export type TaskSelectCreateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    done?: boolean
    title?: boolean
    notes?: boolean
    blocked?: boolean
    objective?: boolean
    date?: boolean
    userId?: boolean
    clientId?: boolean
    user?: boolean | UserDefaultArgs<ExtArgs>
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["task"]>

  export type TaskSelectUpdateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    done?: boolean
    title?: boolean
    notes?: boolean
    blocked?: boolean
    objective?: boolean
    date?: boolean
    userId?: boolean
    clientId?: boolean
    user?: boolean | UserDefaultArgs<ExtArgs>
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["task"]>

  export type TaskSelectScalar = {
    id?: boolean
    done?: boolean
    title?: boolean
    notes?: boolean
    blocked?: boolean
    objective?: boolean
    date?: boolean
    userId?: boolean
    clientId?: boolean
  }

  export type TaskOmit<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetOmit<"id" | "done" | "title" | "notes" | "blocked" | "objective" | "date" | "userId" | "clientId", ExtArgs["result"]["task"]>
  export type TaskInclude<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    user?: boolean | UserDefaultArgs<ExtArgs>
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }
  export type TaskIncludeCreateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    user?: boolean | UserDefaultArgs<ExtArgs>
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }
  export type TaskIncludeUpdateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    user?: boolean | UserDefaultArgs<ExtArgs>
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }

  export type $TaskPayload<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    name: "Task"
    objects: {
      user: Prisma.$UserPayload<ExtArgs>
      client: Prisma.$ClientPayload<ExtArgs>
    }
    scalars: $Extensions.GetPayloadResult<{
      id: number
      done: boolean
      title: string
      notes: string | null
      blocked: string | null
      objective: string | null
      date: Date
      userId: number
      clientId: number
    }, ExtArgs["result"]["task"]>
    composites: {}
  }

  type TaskGetPayload<S extends boolean | null | undefined | TaskDefaultArgs> = $Result.GetResult<Prisma.$TaskPayload, S>

  type TaskCountArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> =
    Omit<TaskFindManyArgs, 'select' | 'include' | 'distinct' | 'omit'> & {
      select?: TaskCountAggregateInputType | true
    }

  export interface TaskDelegate<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs, GlobalOmitOptions = {}> {
    [K: symbol]: { types: Prisma.TypeMap<ExtArgs>['model']['Task'], meta: { name: 'Task' } }
    /**
     * Find zero or one Task that matches the filter.
     * @param {TaskFindUniqueArgs} args - Arguments to find a Task
     * @example
     * // Get one Task
     * const task = await prisma.task.findUnique({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findUnique<T extends TaskFindUniqueArgs>(args: SelectSubset<T, TaskFindUniqueArgs<ExtArgs>>): Prisma__TaskClient<$Result.GetResult<Prisma.$TaskPayload<ExtArgs>, T, "findUnique", GlobalOmitOptions> | null, null, ExtArgs, GlobalOmitOptions>

    /**
     * Find one Task that matches the filter or throw an error with `error.code='P2025'`
     * if no matches were found.
     * @param {TaskFindUniqueOrThrowArgs} args - Arguments to find a Task
     * @example
     * // Get one Task
     * const task = await prisma.task.findUniqueOrThrow({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findUniqueOrThrow<T extends TaskFindUniqueOrThrowArgs>(args: SelectSubset<T, TaskFindUniqueOrThrowArgs<ExtArgs>>): Prisma__TaskClient<$Result.GetResult<Prisma.$TaskPayload<ExtArgs>, T, "findUniqueOrThrow", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Find the first Task that matches the filter.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {TaskFindFirstArgs} args - Arguments to find a Task
     * @example
     * // Get one Task
     * const task = await prisma.task.findFirst({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findFirst<T extends TaskFindFirstArgs>(args?: SelectSubset<T, TaskFindFirstArgs<ExtArgs>>): Prisma__TaskClient<$Result.GetResult<Prisma.$TaskPayload<ExtArgs>, T, "findFirst", GlobalOmitOptions> | null, null, ExtArgs, GlobalOmitOptions>

    /**
     * Find the first Task that matches the filter or
     * throw `PrismaKnownClientError` with `P2025` code if no matches were found.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {TaskFindFirstOrThrowArgs} args - Arguments to find a Task
     * @example
     * // Get one Task
     * const task = await prisma.task.findFirstOrThrow({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findFirstOrThrow<T extends TaskFindFirstOrThrowArgs>(args?: SelectSubset<T, TaskFindFirstOrThrowArgs<ExtArgs>>): Prisma__TaskClient<$Result.GetResult<Prisma.$TaskPayload<ExtArgs>, T, "findFirstOrThrow", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Find zero or more Tasks that matches the filter.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {TaskFindManyArgs} args - Arguments to filter and select certain fields only.
     * @example
     * // Get all Tasks
     * const tasks = await prisma.task.findMany()
     * 
     * // Get first 10 Tasks
     * const tasks = await prisma.task.findMany({ take: 10 })
     * 
     * // Only select the `id`
     * const taskWithIdOnly = await prisma.task.findMany({ select: { id: true } })
     * 
     */
    findMany<T extends TaskFindManyArgs>(args?: SelectSubset<T, TaskFindManyArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$TaskPayload<ExtArgs>, T, "findMany", GlobalOmitOptions>>

    /**
     * Create a Task.
     * @param {TaskCreateArgs} args - Arguments to create a Task.
     * @example
     * // Create one Task
     * const Task = await prisma.task.create({
     *   data: {
     *     // ... data to create a Task
     *   }
     * })
     * 
     */
    create<T extends TaskCreateArgs>(args: SelectSubset<T, TaskCreateArgs<ExtArgs>>): Prisma__TaskClient<$Result.GetResult<Prisma.$TaskPayload<ExtArgs>, T, "create", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Create many Tasks.
     * @param {TaskCreateManyArgs} args - Arguments to create many Tasks.
     * @example
     * // Create many Tasks
     * const task = await prisma.task.createMany({
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     *     
     */
    createMany<T extends TaskCreateManyArgs>(args?: SelectSubset<T, TaskCreateManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Create many Tasks and returns the data saved in the database.
     * @param {TaskCreateManyAndReturnArgs} args - Arguments to create many Tasks.
     * @example
     * // Create many Tasks
     * const task = await prisma.task.createManyAndReturn({
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * 
     * // Create many Tasks and only return the `id`
     * const taskWithIdOnly = await prisma.task.createManyAndReturn({
     *   select: { id: true },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * 
     */
    createManyAndReturn<T extends TaskCreateManyAndReturnArgs>(args?: SelectSubset<T, TaskCreateManyAndReturnArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$TaskPayload<ExtArgs>, T, "createManyAndReturn", GlobalOmitOptions>>

    /**
     * Delete a Task.
     * @param {TaskDeleteArgs} args - Arguments to delete one Task.
     * @example
     * // Delete one Task
     * const Task = await prisma.task.delete({
     *   where: {
     *     // ... filter to delete one Task
     *   }
     * })
     * 
     */
    delete<T extends TaskDeleteArgs>(args: SelectSubset<T, TaskDeleteArgs<ExtArgs>>): Prisma__TaskClient<$Result.GetResult<Prisma.$TaskPayload<ExtArgs>, T, "delete", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Update one Task.
     * @param {TaskUpdateArgs} args - Arguments to update one Task.
     * @example
     * // Update one Task
     * const task = await prisma.task.update({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: {
     *     // ... provide data here
     *   }
     * })
     * 
     */
    update<T extends TaskUpdateArgs>(args: SelectSubset<T, TaskUpdateArgs<ExtArgs>>): Prisma__TaskClient<$Result.GetResult<Prisma.$TaskPayload<ExtArgs>, T, "update", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Delete zero or more Tasks.
     * @param {TaskDeleteManyArgs} args - Arguments to filter Tasks to delete.
     * @example
     * // Delete a few Tasks
     * const { count } = await prisma.task.deleteMany({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     * 
     */
    deleteMany<T extends TaskDeleteManyArgs>(args?: SelectSubset<T, TaskDeleteManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Update zero or more Tasks.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {TaskUpdateManyArgs} args - Arguments to update one or more rows.
     * @example
     * // Update many Tasks
     * const task = await prisma.task.updateMany({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: {
     *     // ... provide data here
     *   }
     * })
     * 
     */
    updateMany<T extends TaskUpdateManyArgs>(args: SelectSubset<T, TaskUpdateManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Update zero or more Tasks and returns the data updated in the database.
     * @param {TaskUpdateManyAndReturnArgs} args - Arguments to update many Tasks.
     * @example
     * // Update many Tasks
     * const task = await prisma.task.updateManyAndReturn({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * 
     * // Update zero or more Tasks and only return the `id`
     * const taskWithIdOnly = await prisma.task.updateManyAndReturn({
     *   select: { id: true },
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * 
     */
    updateManyAndReturn<T extends TaskUpdateManyAndReturnArgs>(args: SelectSubset<T, TaskUpdateManyAndReturnArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$TaskPayload<ExtArgs>, T, "updateManyAndReturn", GlobalOmitOptions>>

    /**
     * Create or update one Task.
     * @param {TaskUpsertArgs} args - Arguments to update or create a Task.
     * @example
     * // Update or create a Task
     * const task = await prisma.task.upsert({
     *   create: {
     *     // ... data to create a Task
     *   },
     *   update: {
     *     // ... in case it already exists, update
     *   },
     *   where: {
     *     // ... the filter for the Task we want to update
     *   }
     * })
     */
    upsert<T extends TaskUpsertArgs>(args: SelectSubset<T, TaskUpsertArgs<ExtArgs>>): Prisma__TaskClient<$Result.GetResult<Prisma.$TaskPayload<ExtArgs>, T, "upsert", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>


    /**
     * Count the number of Tasks.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {TaskCountArgs} args - Arguments to filter Tasks to count.
     * @example
     * // Count the number of Tasks
     * const count = await prisma.task.count({
     *   where: {
     *     // ... the filter for the Tasks we want to count
     *   }
     * })
    **/
    count<T extends TaskCountArgs>(
      args?: Subset<T, TaskCountArgs>,
    ): Prisma.PrismaPromise<
      T extends $Utils.Record<'select', any>
        ? T['select'] extends true
          ? number
          : GetScalarType<T['select'], TaskCountAggregateOutputType>
        : number
    >

    /**
     * Allows you to perform aggregations operations on a Task.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {TaskAggregateArgs} args - Select which aggregations you would like to apply and on what fields.
     * @example
     * // Ordered by age ascending
     * // Where email contains prisma.io
     * // Limited to the 10 users
     * const aggregations = await prisma.user.aggregate({
     *   _avg: {
     *     age: true,
     *   },
     *   where: {
     *     email: {
     *       contains: "prisma.io",
     *     },
     *   },
     *   orderBy: {
     *     age: "asc",
     *   },
     *   take: 10,
     * })
    **/
    aggregate<T extends TaskAggregateArgs>(args: Subset<T, TaskAggregateArgs>): Prisma.PrismaPromise<GetTaskAggregateType<T>>

    /**
     * Group by Task.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {TaskGroupByArgs} args - Group by arguments.
     * @example
     * // Group by city, order by createdAt, get count
     * const result = await prisma.user.groupBy({
     *   by: ['city', 'createdAt'],
     *   orderBy: {
     *     createdAt: true
     *   },
     *   _count: {
     *     _all: true
     *   },
     * })
     * 
    **/
    groupBy<
      T extends TaskGroupByArgs,
      HasSelectOrTake extends Or<
        Extends<'skip', Keys<T>>,
        Extends<'take', Keys<T>>
      >,
      OrderByArg extends True extends HasSelectOrTake
        ? { orderBy: TaskGroupByArgs['orderBy'] }
        : { orderBy?: TaskGroupByArgs['orderBy'] },
      OrderFields extends ExcludeUnderscoreKeys<Keys<MaybeTupleToUnion<T['orderBy']>>>,
      ByFields extends MaybeTupleToUnion<T['by']>,
      ByValid extends Has<ByFields, OrderFields>,
      HavingFields extends GetHavingFields<T['having']>,
      HavingValid extends Has<ByFields, HavingFields>,
      ByEmpty extends T['by'] extends never[] ? True : False,
      InputErrors extends ByEmpty extends True
      ? `Error: "by" must not be empty.`
      : HavingValid extends False
      ? {
          [P in HavingFields]: P extends ByFields
            ? never
            : P extends string
            ? `Error: Field "${P}" used in "having" needs to be provided in "by".`
            : [
                Error,
                'Field ',
                P,
                ` in "having" needs to be provided in "by"`,
              ]
        }[HavingFields]
      : 'take' extends Keys<T>
      ? 'orderBy' extends Keys<T>
        ? ByValid extends True
          ? {}
          : {
              [P in OrderFields]: P extends ByFields
                ? never
                : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
            }[OrderFields]
        : 'Error: If you provide "take", you also need to provide "orderBy"'
      : 'skip' extends Keys<T>
      ? 'orderBy' extends Keys<T>
        ? ByValid extends True
          ? {}
          : {
              [P in OrderFields]: P extends ByFields
                ? never
                : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
            }[OrderFields]
        : 'Error: If you provide "skip", you also need to provide "orderBy"'
      : ByValid extends True
      ? {}
      : {
          [P in OrderFields]: P extends ByFields
            ? never
            : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
        }[OrderFields]
    >(args: SubsetIntersection<T, TaskGroupByArgs, OrderByArg> & InputErrors): {} extends InputErrors ? GetTaskGroupByPayload<T> : Prisma.PrismaPromise<InputErrors>
  /**
   * Fields of the Task model
   */
  readonly fields: TaskFieldRefs;
  }

  /**
   * The delegate class that acts as a "Promise-like" for Task.
   * Why is this prefixed with `Prisma__`?
   * Because we want to prevent naming conflicts as mentioned in
   * https://github.com/prisma/prisma-client-js/issues/707
   */
  export interface Prisma__TaskClient<T, Null = never, ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs, GlobalOmitOptions = {}> extends Prisma.PrismaPromise<T> {
    readonly [Symbol.toStringTag]: "PrismaPromise"
    user<T extends UserDefaultArgs<ExtArgs> = {}>(args?: Subset<T, UserDefaultArgs<ExtArgs>>): Prisma__UserClient<$Result.GetResult<Prisma.$UserPayload<ExtArgs>, T, "findUniqueOrThrow", GlobalOmitOptions> | Null, Null, ExtArgs, GlobalOmitOptions>
    client<T extends ClientDefaultArgs<ExtArgs> = {}>(args?: Subset<T, ClientDefaultArgs<ExtArgs>>): Prisma__ClientClient<$Result.GetResult<Prisma.$ClientPayload<ExtArgs>, T, "findUniqueOrThrow", GlobalOmitOptions> | Null, Null, ExtArgs, GlobalOmitOptions>
    /**
     * Attaches callbacks for the resolution and/or rejection of the Promise.
     * @param onfulfilled The callback to execute when the Promise is resolved.
     * @param onrejected The callback to execute when the Promise is rejected.
     * @returns A Promise for the completion of which ever callback is executed.
     */
    then<TResult1 = T, TResult2 = never>(onfulfilled?: ((value: T) => TResult1 | PromiseLike<TResult1>) | undefined | null, onrejected?: ((reason: any) => TResult2 | PromiseLike<TResult2>) | undefined | null): $Utils.JsPromise<TResult1 | TResult2>
    /**
     * Attaches a callback for only the rejection of the Promise.
     * @param onrejected The callback to execute when the Promise is rejected.
     * @returns A Promise for the completion of the callback.
     */
    catch<TResult = never>(onrejected?: ((reason: any) => TResult | PromiseLike<TResult>) | undefined | null): $Utils.JsPromise<T | TResult>
    /**
     * Attaches a callback that is invoked when the Promise is settled (fulfilled or rejected). The
     * resolved value cannot be modified from the callback.
     * @param onfinally The callback to execute when the Promise is settled (fulfilled or rejected).
     * @returns A Promise for the completion of the callback.
     */
    finally(onfinally?: (() => void) | undefined | null): $Utils.JsPromise<T>
  }




  /**
   * Fields of the Task model
   */
  interface TaskFieldRefs {
    readonly id: FieldRef<"Task", 'Int'>
    readonly done: FieldRef<"Task", 'Boolean'>
    readonly title: FieldRef<"Task", 'String'>
    readonly notes: FieldRef<"Task", 'String'>
    readonly blocked: FieldRef<"Task", 'String'>
    readonly objective: FieldRef<"Task", 'String'>
    readonly date: FieldRef<"Task", 'DateTime'>
    readonly userId: FieldRef<"Task", 'Int'>
    readonly clientId: FieldRef<"Task", 'Int'>
  }
    

  // Custom InputTypes
  /**
   * Task findUnique
   */
  export type TaskFindUniqueArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Task
     */
    select?: TaskSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Task
     */
    omit?: TaskOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: TaskInclude<ExtArgs> | null
    /**
     * Filter, which Task to fetch.
     */
    where: TaskWhereUniqueInput
  }

  /**
   * Task findUniqueOrThrow
   */
  export type TaskFindUniqueOrThrowArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Task
     */
    select?: TaskSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Task
     */
    omit?: TaskOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: TaskInclude<ExtArgs> | null
    /**
     * Filter, which Task to fetch.
     */
    where: TaskWhereUniqueInput
  }

  /**
   * Task findFirst
   */
  export type TaskFindFirstArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Task
     */
    select?: TaskSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Task
     */
    omit?: TaskOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: TaskInclude<ExtArgs> | null
    /**
     * Filter, which Task to fetch.
     */
    where?: TaskWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Tasks to fetch.
     */
    orderBy?: TaskOrderByWithRelationInput | TaskOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for searching for Tasks.
     */
    cursor?: TaskWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Tasks from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Tasks.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/distinct Distinct Docs}
     * 
     * Filter by unique combinations of Tasks.
     */
    distinct?: TaskScalarFieldEnum | TaskScalarFieldEnum[]
  }

  /**
   * Task findFirstOrThrow
   */
  export type TaskFindFirstOrThrowArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Task
     */
    select?: TaskSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Task
     */
    omit?: TaskOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: TaskInclude<ExtArgs> | null
    /**
     * Filter, which Task to fetch.
     */
    where?: TaskWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Tasks to fetch.
     */
    orderBy?: TaskOrderByWithRelationInput | TaskOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for searching for Tasks.
     */
    cursor?: TaskWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Tasks from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Tasks.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/distinct Distinct Docs}
     * 
     * Filter by unique combinations of Tasks.
     */
    distinct?: TaskScalarFieldEnum | TaskScalarFieldEnum[]
  }

  /**
   * Task findMany
   */
  export type TaskFindManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Task
     */
    select?: TaskSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Task
     */
    omit?: TaskOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: TaskInclude<ExtArgs> | null
    /**
     * Filter, which Tasks to fetch.
     */
    where?: TaskWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Tasks to fetch.
     */
    orderBy?: TaskOrderByWithRelationInput | TaskOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for listing Tasks.
     */
    cursor?: TaskWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Tasks from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Tasks.
     */
    skip?: number
    distinct?: TaskScalarFieldEnum | TaskScalarFieldEnum[]
  }

  /**
   * Task create
   */
  export type TaskCreateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Task
     */
    select?: TaskSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Task
     */
    omit?: TaskOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: TaskInclude<ExtArgs> | null
    /**
     * The data needed to create a Task.
     */
    data: XOR<TaskCreateInput, TaskUncheckedCreateInput>
  }

  /**
   * Task createMany
   */
  export type TaskCreateManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * The data used to create many Tasks.
     */
    data: TaskCreateManyInput | TaskCreateManyInput[]
    skipDuplicates?: boolean
  }

  /**
   * Task createManyAndReturn
   */
  export type TaskCreateManyAndReturnArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Task
     */
    select?: TaskSelectCreateManyAndReturn<ExtArgs> | null
    /**
     * Omit specific fields from the Task
     */
    omit?: TaskOmit<ExtArgs> | null
    /**
     * The data used to create many Tasks.
     */
    data: TaskCreateManyInput | TaskCreateManyInput[]
    skipDuplicates?: boolean
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: TaskIncludeCreateManyAndReturn<ExtArgs> | null
  }

  /**
   * Task update
   */
  export type TaskUpdateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Task
     */
    select?: TaskSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Task
     */
    omit?: TaskOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: TaskInclude<ExtArgs> | null
    /**
     * The data needed to update a Task.
     */
    data: XOR<TaskUpdateInput, TaskUncheckedUpdateInput>
    /**
     * Choose, which Task to update.
     */
    where: TaskWhereUniqueInput
  }

  /**
   * Task updateMany
   */
  export type TaskUpdateManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * The data used to update Tasks.
     */
    data: XOR<TaskUpdateManyMutationInput, TaskUncheckedUpdateManyInput>
    /**
     * Filter which Tasks to update
     */
    where?: TaskWhereInput
    /**
     * Limit how many Tasks to update.
     */
    limit?: number
  }

  /**
   * Task updateManyAndReturn
   */
  export type TaskUpdateManyAndReturnArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Task
     */
    select?: TaskSelectUpdateManyAndReturn<ExtArgs> | null
    /**
     * Omit specific fields from the Task
     */
    omit?: TaskOmit<ExtArgs> | null
    /**
     * The data used to update Tasks.
     */
    data: XOR<TaskUpdateManyMutationInput, TaskUncheckedUpdateManyInput>
    /**
     * Filter which Tasks to update
     */
    where?: TaskWhereInput
    /**
     * Limit how many Tasks to update.
     */
    limit?: number
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: TaskIncludeUpdateManyAndReturn<ExtArgs> | null
  }

  /**
   * Task upsert
   */
  export type TaskUpsertArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Task
     */
    select?: TaskSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Task
     */
    omit?: TaskOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: TaskInclude<ExtArgs> | null
    /**
     * The filter to search for the Task to update in case it exists.
     */
    where: TaskWhereUniqueInput
    /**
     * In case the Task found by the `where` argument doesn't exist, create a new Task with this data.
     */
    create: XOR<TaskCreateInput, TaskUncheckedCreateInput>
    /**
     * In case the Task was found with the provided `where` argument, update it with this data.
     */
    update: XOR<TaskUpdateInput, TaskUncheckedUpdateInput>
  }

  /**
   * Task delete
   */
  export type TaskDeleteArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Task
     */
    select?: TaskSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Task
     */
    omit?: TaskOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: TaskInclude<ExtArgs> | null
    /**
     * Filter which Task to delete.
     */
    where: TaskWhereUniqueInput
  }

  /**
   * Task deleteMany
   */
  export type TaskDeleteManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Filter which Tasks to delete
     */
    where?: TaskWhereInput
    /**
     * Limit how many Tasks to delete.
     */
    limit?: number
  }

  /**
   * Task without action
   */
  export type TaskDefaultArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Task
     */
    select?: TaskSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Task
     */
    omit?: TaskOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: TaskInclude<ExtArgs> | null
  }


  /**
   * Model Rule
   */

  export type AggregateRule = {
    _count: RuleCountAggregateOutputType | null
    _avg: RuleAvgAggregateOutputType | null
    _sum: RuleSumAggregateOutputType | null
    _min: RuleMinAggregateOutputType | null
    _max: RuleMaxAggregateOutputType | null
  }

  export type RuleAvgAggregateOutputType = {
    id: number | null
  }

  export type RuleSumAggregateOutputType = {
    id: number | null
  }

  export type RuleMinAggregateOutputType = {
    id: number | null
    name: string | null
    description: string | null
    frequency: $Enums.Frequency | null
    taskTitle: string | null
    taskNotes: string | null
    active: boolean | null
    createdAt: Date | null
    updatedAt: Date | null
  }

  export type RuleMaxAggregateOutputType = {
    id: number | null
    name: string | null
    description: string | null
    frequency: $Enums.Frequency | null
    taskTitle: string | null
    taskNotes: string | null
    active: boolean | null
    createdAt: Date | null
    updatedAt: Date | null
  }

  export type RuleCountAggregateOutputType = {
    id: number
    name: number
    description: number
    frequency: number
    taskTitle: number
    taskNotes: number
    active: number
    createdAt: number
    updatedAt: number
    _all: number
  }


  export type RuleAvgAggregateInputType = {
    id?: true
  }

  export type RuleSumAggregateInputType = {
    id?: true
  }

  export type RuleMinAggregateInputType = {
    id?: true
    name?: true
    description?: true
    frequency?: true
    taskTitle?: true
    taskNotes?: true
    active?: true
    createdAt?: true
    updatedAt?: true
  }

  export type RuleMaxAggregateInputType = {
    id?: true
    name?: true
    description?: true
    frequency?: true
    taskTitle?: true
    taskNotes?: true
    active?: true
    createdAt?: true
    updatedAt?: true
  }

  export type RuleCountAggregateInputType = {
    id?: true
    name?: true
    description?: true
    frequency?: true
    taskTitle?: true
    taskNotes?: true
    active?: true
    createdAt?: true
    updatedAt?: true
    _all?: true
  }

  export type RuleAggregateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Filter which Rule to aggregate.
     */
    where?: RuleWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Rules to fetch.
     */
    orderBy?: RuleOrderByWithRelationInput | RuleOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the start position
     */
    cursor?: RuleWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Rules from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Rules.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Count returned Rules
    **/
    _count?: true | RuleCountAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to average
    **/
    _avg?: RuleAvgAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to sum
    **/
    _sum?: RuleSumAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to find the minimum value
    **/
    _min?: RuleMinAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to find the maximum value
    **/
    _max?: RuleMaxAggregateInputType
  }

  export type GetRuleAggregateType<T extends RuleAggregateArgs> = {
        [P in keyof T & keyof AggregateRule]: P extends '_count' | 'count'
      ? T[P] extends true
        ? number
        : GetScalarType<T[P], AggregateRule[P]>
      : GetScalarType<T[P], AggregateRule[P]>
  }




  export type RuleGroupByArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    where?: RuleWhereInput
    orderBy?: RuleOrderByWithAggregationInput | RuleOrderByWithAggregationInput[]
    by: RuleScalarFieldEnum[] | RuleScalarFieldEnum
    having?: RuleScalarWhereWithAggregatesInput
    take?: number
    skip?: number
    _count?: RuleCountAggregateInputType | true
    _avg?: RuleAvgAggregateInputType
    _sum?: RuleSumAggregateInputType
    _min?: RuleMinAggregateInputType
    _max?: RuleMaxAggregateInputType
  }

  export type RuleGroupByOutputType = {
    id: number
    name: string
    description: string | null
    frequency: $Enums.Frequency
    taskTitle: string
    taskNotes: string | null
    active: boolean
    createdAt: Date
    updatedAt: Date
    _count: RuleCountAggregateOutputType | null
    _avg: RuleAvgAggregateOutputType | null
    _sum: RuleSumAggregateOutputType | null
    _min: RuleMinAggregateOutputType | null
    _max: RuleMaxAggregateOutputType | null
  }

  type GetRuleGroupByPayload<T extends RuleGroupByArgs> = Prisma.PrismaPromise<
    Array<
      PickEnumerable<RuleGroupByOutputType, T['by']> &
        {
          [P in ((keyof T) & (keyof RuleGroupByOutputType))]: P extends '_count'
            ? T[P] extends boolean
              ? number
              : GetScalarType<T[P], RuleGroupByOutputType[P]>
            : GetScalarType<T[P], RuleGroupByOutputType[P]>
        }
      >
    >


  export type RuleSelect<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    name?: boolean
    description?: boolean
    frequency?: boolean
    taskTitle?: boolean
    taskNotes?: boolean
    active?: boolean
    createdAt?: boolean
    updatedAt?: boolean
    conditions?: boolean | Rule$conditionsArgs<ExtArgs>
    _count?: boolean | RuleCountOutputTypeDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["rule"]>

  export type RuleSelectCreateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    name?: boolean
    description?: boolean
    frequency?: boolean
    taskTitle?: boolean
    taskNotes?: boolean
    active?: boolean
    createdAt?: boolean
    updatedAt?: boolean
  }, ExtArgs["result"]["rule"]>

  export type RuleSelectUpdateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    name?: boolean
    description?: boolean
    frequency?: boolean
    taskTitle?: boolean
    taskNotes?: boolean
    active?: boolean
    createdAt?: boolean
    updatedAt?: boolean
  }, ExtArgs["result"]["rule"]>

  export type RuleSelectScalar = {
    id?: boolean
    name?: boolean
    description?: boolean
    frequency?: boolean
    taskTitle?: boolean
    taskNotes?: boolean
    active?: boolean
    createdAt?: boolean
    updatedAt?: boolean
  }

  export type RuleOmit<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetOmit<"id" | "name" | "description" | "frequency" | "taskTitle" | "taskNotes" | "active" | "createdAt" | "updatedAt", ExtArgs["result"]["rule"]>
  export type RuleInclude<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    conditions?: boolean | Rule$conditionsArgs<ExtArgs>
    _count?: boolean | RuleCountOutputTypeDefaultArgs<ExtArgs>
  }
  export type RuleIncludeCreateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {}
  export type RuleIncludeUpdateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {}

  export type $RulePayload<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    name: "Rule"
    objects: {
      conditions: Prisma.$RuleConditionPayload<ExtArgs>[]
    }
    scalars: $Extensions.GetPayloadResult<{
      id: number
      name: string
      description: string | null
      frequency: $Enums.Frequency
      taskTitle: string
      taskNotes: string | null
      active: boolean
      createdAt: Date
      updatedAt: Date
    }, ExtArgs["result"]["rule"]>
    composites: {}
  }

  type RuleGetPayload<S extends boolean | null | undefined | RuleDefaultArgs> = $Result.GetResult<Prisma.$RulePayload, S>

  type RuleCountArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> =
    Omit<RuleFindManyArgs, 'select' | 'include' | 'distinct' | 'omit'> & {
      select?: RuleCountAggregateInputType | true
    }

  export interface RuleDelegate<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs, GlobalOmitOptions = {}> {
    [K: symbol]: { types: Prisma.TypeMap<ExtArgs>['model']['Rule'], meta: { name: 'Rule' } }
    /**
     * Find zero or one Rule that matches the filter.
     * @param {RuleFindUniqueArgs} args - Arguments to find a Rule
     * @example
     * // Get one Rule
     * const rule = await prisma.rule.findUnique({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findUnique<T extends RuleFindUniqueArgs>(args: SelectSubset<T, RuleFindUniqueArgs<ExtArgs>>): Prisma__RuleClient<$Result.GetResult<Prisma.$RulePayload<ExtArgs>, T, "findUnique", GlobalOmitOptions> | null, null, ExtArgs, GlobalOmitOptions>

    /**
     * Find one Rule that matches the filter or throw an error with `error.code='P2025'`
     * if no matches were found.
     * @param {RuleFindUniqueOrThrowArgs} args - Arguments to find a Rule
     * @example
     * // Get one Rule
     * const rule = await prisma.rule.findUniqueOrThrow({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findUniqueOrThrow<T extends RuleFindUniqueOrThrowArgs>(args: SelectSubset<T, RuleFindUniqueOrThrowArgs<ExtArgs>>): Prisma__RuleClient<$Result.GetResult<Prisma.$RulePayload<ExtArgs>, T, "findUniqueOrThrow", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Find the first Rule that matches the filter.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {RuleFindFirstArgs} args - Arguments to find a Rule
     * @example
     * // Get one Rule
     * const rule = await prisma.rule.findFirst({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findFirst<T extends RuleFindFirstArgs>(args?: SelectSubset<T, RuleFindFirstArgs<ExtArgs>>): Prisma__RuleClient<$Result.GetResult<Prisma.$RulePayload<ExtArgs>, T, "findFirst", GlobalOmitOptions> | null, null, ExtArgs, GlobalOmitOptions>

    /**
     * Find the first Rule that matches the filter or
     * throw `PrismaKnownClientError` with `P2025` code if no matches were found.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {RuleFindFirstOrThrowArgs} args - Arguments to find a Rule
     * @example
     * // Get one Rule
     * const rule = await prisma.rule.findFirstOrThrow({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findFirstOrThrow<T extends RuleFindFirstOrThrowArgs>(args?: SelectSubset<T, RuleFindFirstOrThrowArgs<ExtArgs>>): Prisma__RuleClient<$Result.GetResult<Prisma.$RulePayload<ExtArgs>, T, "findFirstOrThrow", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Find zero or more Rules that matches the filter.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {RuleFindManyArgs} args - Arguments to filter and select certain fields only.
     * @example
     * // Get all Rules
     * const rules = await prisma.rule.findMany()
     * 
     * // Get first 10 Rules
     * const rules = await prisma.rule.findMany({ take: 10 })
     * 
     * // Only select the `id`
     * const ruleWithIdOnly = await prisma.rule.findMany({ select: { id: true } })
     * 
     */
    findMany<T extends RuleFindManyArgs>(args?: SelectSubset<T, RuleFindManyArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$RulePayload<ExtArgs>, T, "findMany", GlobalOmitOptions>>

    /**
     * Create a Rule.
     * @param {RuleCreateArgs} args - Arguments to create a Rule.
     * @example
     * // Create one Rule
     * const Rule = await prisma.rule.create({
     *   data: {
     *     // ... data to create a Rule
     *   }
     * })
     * 
     */
    create<T extends RuleCreateArgs>(args: SelectSubset<T, RuleCreateArgs<ExtArgs>>): Prisma__RuleClient<$Result.GetResult<Prisma.$RulePayload<ExtArgs>, T, "create", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Create many Rules.
     * @param {RuleCreateManyArgs} args - Arguments to create many Rules.
     * @example
     * // Create many Rules
     * const rule = await prisma.rule.createMany({
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     *     
     */
    createMany<T extends RuleCreateManyArgs>(args?: SelectSubset<T, RuleCreateManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Create many Rules and returns the data saved in the database.
     * @param {RuleCreateManyAndReturnArgs} args - Arguments to create many Rules.
     * @example
     * // Create many Rules
     * const rule = await prisma.rule.createManyAndReturn({
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * 
     * // Create many Rules and only return the `id`
     * const ruleWithIdOnly = await prisma.rule.createManyAndReturn({
     *   select: { id: true },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * 
     */
    createManyAndReturn<T extends RuleCreateManyAndReturnArgs>(args?: SelectSubset<T, RuleCreateManyAndReturnArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$RulePayload<ExtArgs>, T, "createManyAndReturn", GlobalOmitOptions>>

    /**
     * Delete a Rule.
     * @param {RuleDeleteArgs} args - Arguments to delete one Rule.
     * @example
     * // Delete one Rule
     * const Rule = await prisma.rule.delete({
     *   where: {
     *     // ... filter to delete one Rule
     *   }
     * })
     * 
     */
    delete<T extends RuleDeleteArgs>(args: SelectSubset<T, RuleDeleteArgs<ExtArgs>>): Prisma__RuleClient<$Result.GetResult<Prisma.$RulePayload<ExtArgs>, T, "delete", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Update one Rule.
     * @param {RuleUpdateArgs} args - Arguments to update one Rule.
     * @example
     * // Update one Rule
     * const rule = await prisma.rule.update({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: {
     *     // ... provide data here
     *   }
     * })
     * 
     */
    update<T extends RuleUpdateArgs>(args: SelectSubset<T, RuleUpdateArgs<ExtArgs>>): Prisma__RuleClient<$Result.GetResult<Prisma.$RulePayload<ExtArgs>, T, "update", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Delete zero or more Rules.
     * @param {RuleDeleteManyArgs} args - Arguments to filter Rules to delete.
     * @example
     * // Delete a few Rules
     * const { count } = await prisma.rule.deleteMany({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     * 
     */
    deleteMany<T extends RuleDeleteManyArgs>(args?: SelectSubset<T, RuleDeleteManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Update zero or more Rules.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {RuleUpdateManyArgs} args - Arguments to update one or more rows.
     * @example
     * // Update many Rules
     * const rule = await prisma.rule.updateMany({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: {
     *     // ... provide data here
     *   }
     * })
     * 
     */
    updateMany<T extends RuleUpdateManyArgs>(args: SelectSubset<T, RuleUpdateManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Update zero or more Rules and returns the data updated in the database.
     * @param {RuleUpdateManyAndReturnArgs} args - Arguments to update many Rules.
     * @example
     * // Update many Rules
     * const rule = await prisma.rule.updateManyAndReturn({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * 
     * // Update zero or more Rules and only return the `id`
     * const ruleWithIdOnly = await prisma.rule.updateManyAndReturn({
     *   select: { id: true },
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * 
     */
    updateManyAndReturn<T extends RuleUpdateManyAndReturnArgs>(args: SelectSubset<T, RuleUpdateManyAndReturnArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$RulePayload<ExtArgs>, T, "updateManyAndReturn", GlobalOmitOptions>>

    /**
     * Create or update one Rule.
     * @param {RuleUpsertArgs} args - Arguments to update or create a Rule.
     * @example
     * // Update or create a Rule
     * const rule = await prisma.rule.upsert({
     *   create: {
     *     // ... data to create a Rule
     *   },
     *   update: {
     *     // ... in case it already exists, update
     *   },
     *   where: {
     *     // ... the filter for the Rule we want to update
     *   }
     * })
     */
    upsert<T extends RuleUpsertArgs>(args: SelectSubset<T, RuleUpsertArgs<ExtArgs>>): Prisma__RuleClient<$Result.GetResult<Prisma.$RulePayload<ExtArgs>, T, "upsert", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>


    /**
     * Count the number of Rules.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {RuleCountArgs} args - Arguments to filter Rules to count.
     * @example
     * // Count the number of Rules
     * const count = await prisma.rule.count({
     *   where: {
     *     // ... the filter for the Rules we want to count
     *   }
     * })
    **/
    count<T extends RuleCountArgs>(
      args?: Subset<T, RuleCountArgs>,
    ): Prisma.PrismaPromise<
      T extends $Utils.Record<'select', any>
        ? T['select'] extends true
          ? number
          : GetScalarType<T['select'], RuleCountAggregateOutputType>
        : number
    >

    /**
     * Allows you to perform aggregations operations on a Rule.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {RuleAggregateArgs} args - Select which aggregations you would like to apply and on what fields.
     * @example
     * // Ordered by age ascending
     * // Where email contains prisma.io
     * // Limited to the 10 users
     * const aggregations = await prisma.user.aggregate({
     *   _avg: {
     *     age: true,
     *   },
     *   where: {
     *     email: {
     *       contains: "prisma.io",
     *     },
     *   },
     *   orderBy: {
     *     age: "asc",
     *   },
     *   take: 10,
     * })
    **/
    aggregate<T extends RuleAggregateArgs>(args: Subset<T, RuleAggregateArgs>): Prisma.PrismaPromise<GetRuleAggregateType<T>>

    /**
     * Group by Rule.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {RuleGroupByArgs} args - Group by arguments.
     * @example
     * // Group by city, order by createdAt, get count
     * const result = await prisma.user.groupBy({
     *   by: ['city', 'createdAt'],
     *   orderBy: {
     *     createdAt: true
     *   },
     *   _count: {
     *     _all: true
     *   },
     * })
     * 
    **/
    groupBy<
      T extends RuleGroupByArgs,
      HasSelectOrTake extends Or<
        Extends<'skip', Keys<T>>,
        Extends<'take', Keys<T>>
      >,
      OrderByArg extends True extends HasSelectOrTake
        ? { orderBy: RuleGroupByArgs['orderBy'] }
        : { orderBy?: RuleGroupByArgs['orderBy'] },
      OrderFields extends ExcludeUnderscoreKeys<Keys<MaybeTupleToUnion<T['orderBy']>>>,
      ByFields extends MaybeTupleToUnion<T['by']>,
      ByValid extends Has<ByFields, OrderFields>,
      HavingFields extends GetHavingFields<T['having']>,
      HavingValid extends Has<ByFields, HavingFields>,
      ByEmpty extends T['by'] extends never[] ? True : False,
      InputErrors extends ByEmpty extends True
      ? `Error: "by" must not be empty.`
      : HavingValid extends False
      ? {
          [P in HavingFields]: P extends ByFields
            ? never
            : P extends string
            ? `Error: Field "${P}" used in "having" needs to be provided in "by".`
            : [
                Error,
                'Field ',
                P,
                ` in "having" needs to be provided in "by"`,
              ]
        }[HavingFields]
      : 'take' extends Keys<T>
      ? 'orderBy' extends Keys<T>
        ? ByValid extends True
          ? {}
          : {
              [P in OrderFields]: P extends ByFields
                ? never
                : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
            }[OrderFields]
        : 'Error: If you provide "take", you also need to provide "orderBy"'
      : 'skip' extends Keys<T>
      ? 'orderBy' extends Keys<T>
        ? ByValid extends True
          ? {}
          : {
              [P in OrderFields]: P extends ByFields
                ? never
                : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
            }[OrderFields]
        : 'Error: If you provide "skip", you also need to provide "orderBy"'
      : ByValid extends True
      ? {}
      : {
          [P in OrderFields]: P extends ByFields
            ? never
            : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
        }[OrderFields]
    >(args: SubsetIntersection<T, RuleGroupByArgs, OrderByArg> & InputErrors): {} extends InputErrors ? GetRuleGroupByPayload<T> : Prisma.PrismaPromise<InputErrors>
  /**
   * Fields of the Rule model
   */
  readonly fields: RuleFieldRefs;
  }

  /**
   * The delegate class that acts as a "Promise-like" for Rule.
   * Why is this prefixed with `Prisma__`?
   * Because we want to prevent naming conflicts as mentioned in
   * https://github.com/prisma/prisma-client-js/issues/707
   */
  export interface Prisma__RuleClient<T, Null = never, ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs, GlobalOmitOptions = {}> extends Prisma.PrismaPromise<T> {
    readonly [Symbol.toStringTag]: "PrismaPromise"
    conditions<T extends Rule$conditionsArgs<ExtArgs> = {}>(args?: Subset<T, Rule$conditionsArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$RuleConditionPayload<ExtArgs>, T, "findMany", GlobalOmitOptions> | Null>
    /**
     * Attaches callbacks for the resolution and/or rejection of the Promise.
     * @param onfulfilled The callback to execute when the Promise is resolved.
     * @param onrejected The callback to execute when the Promise is rejected.
     * @returns A Promise for the completion of which ever callback is executed.
     */
    then<TResult1 = T, TResult2 = never>(onfulfilled?: ((value: T) => TResult1 | PromiseLike<TResult1>) | undefined | null, onrejected?: ((reason: any) => TResult2 | PromiseLike<TResult2>) | undefined | null): $Utils.JsPromise<TResult1 | TResult2>
    /**
     * Attaches a callback for only the rejection of the Promise.
     * @param onrejected The callback to execute when the Promise is rejected.
     * @returns A Promise for the completion of the callback.
     */
    catch<TResult = never>(onrejected?: ((reason: any) => TResult | PromiseLike<TResult>) | undefined | null): $Utils.JsPromise<T | TResult>
    /**
     * Attaches a callback that is invoked when the Promise is settled (fulfilled or rejected). The
     * resolved value cannot be modified from the callback.
     * @param onfinally The callback to execute when the Promise is settled (fulfilled or rejected).
     * @returns A Promise for the completion of the callback.
     */
    finally(onfinally?: (() => void) | undefined | null): $Utils.JsPromise<T>
  }




  /**
   * Fields of the Rule model
   */
  interface RuleFieldRefs {
    readonly id: FieldRef<"Rule", 'Int'>
    readonly name: FieldRef<"Rule", 'String'>
    readonly description: FieldRef<"Rule", 'String'>
    readonly frequency: FieldRef<"Rule", 'Frequency'>
    readonly taskTitle: FieldRef<"Rule", 'String'>
    readonly taskNotes: FieldRef<"Rule", 'String'>
    readonly active: FieldRef<"Rule", 'Boolean'>
    readonly createdAt: FieldRef<"Rule", 'DateTime'>
    readonly updatedAt: FieldRef<"Rule", 'DateTime'>
  }
    

  // Custom InputTypes
  /**
   * Rule findUnique
   */
  export type RuleFindUniqueArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Rule
     */
    select?: RuleSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Rule
     */
    omit?: RuleOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleInclude<ExtArgs> | null
    /**
     * Filter, which Rule to fetch.
     */
    where: RuleWhereUniqueInput
  }

  /**
   * Rule findUniqueOrThrow
   */
  export type RuleFindUniqueOrThrowArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Rule
     */
    select?: RuleSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Rule
     */
    omit?: RuleOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleInclude<ExtArgs> | null
    /**
     * Filter, which Rule to fetch.
     */
    where: RuleWhereUniqueInput
  }

  /**
   * Rule findFirst
   */
  export type RuleFindFirstArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Rule
     */
    select?: RuleSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Rule
     */
    omit?: RuleOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleInclude<ExtArgs> | null
    /**
     * Filter, which Rule to fetch.
     */
    where?: RuleWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Rules to fetch.
     */
    orderBy?: RuleOrderByWithRelationInput | RuleOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for searching for Rules.
     */
    cursor?: RuleWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Rules from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Rules.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/distinct Distinct Docs}
     * 
     * Filter by unique combinations of Rules.
     */
    distinct?: RuleScalarFieldEnum | RuleScalarFieldEnum[]
  }

  /**
   * Rule findFirstOrThrow
   */
  export type RuleFindFirstOrThrowArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Rule
     */
    select?: RuleSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Rule
     */
    omit?: RuleOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleInclude<ExtArgs> | null
    /**
     * Filter, which Rule to fetch.
     */
    where?: RuleWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Rules to fetch.
     */
    orderBy?: RuleOrderByWithRelationInput | RuleOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for searching for Rules.
     */
    cursor?: RuleWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Rules from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Rules.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/distinct Distinct Docs}
     * 
     * Filter by unique combinations of Rules.
     */
    distinct?: RuleScalarFieldEnum | RuleScalarFieldEnum[]
  }

  /**
   * Rule findMany
   */
  export type RuleFindManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Rule
     */
    select?: RuleSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Rule
     */
    omit?: RuleOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleInclude<ExtArgs> | null
    /**
     * Filter, which Rules to fetch.
     */
    where?: RuleWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of Rules to fetch.
     */
    orderBy?: RuleOrderByWithRelationInput | RuleOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for listing Rules.
     */
    cursor?: RuleWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` Rules from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` Rules.
     */
    skip?: number
    distinct?: RuleScalarFieldEnum | RuleScalarFieldEnum[]
  }

  /**
   * Rule create
   */
  export type RuleCreateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Rule
     */
    select?: RuleSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Rule
     */
    omit?: RuleOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleInclude<ExtArgs> | null
    /**
     * The data needed to create a Rule.
     */
    data: XOR<RuleCreateInput, RuleUncheckedCreateInput>
  }

  /**
   * Rule createMany
   */
  export type RuleCreateManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * The data used to create many Rules.
     */
    data: RuleCreateManyInput | RuleCreateManyInput[]
    skipDuplicates?: boolean
  }

  /**
   * Rule createManyAndReturn
   */
  export type RuleCreateManyAndReturnArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Rule
     */
    select?: RuleSelectCreateManyAndReturn<ExtArgs> | null
    /**
     * Omit specific fields from the Rule
     */
    omit?: RuleOmit<ExtArgs> | null
    /**
     * The data used to create many Rules.
     */
    data: RuleCreateManyInput | RuleCreateManyInput[]
    skipDuplicates?: boolean
  }

  /**
   * Rule update
   */
  export type RuleUpdateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Rule
     */
    select?: RuleSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Rule
     */
    omit?: RuleOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleInclude<ExtArgs> | null
    /**
     * The data needed to update a Rule.
     */
    data: XOR<RuleUpdateInput, RuleUncheckedUpdateInput>
    /**
     * Choose, which Rule to update.
     */
    where: RuleWhereUniqueInput
  }

  /**
   * Rule updateMany
   */
  export type RuleUpdateManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * The data used to update Rules.
     */
    data: XOR<RuleUpdateManyMutationInput, RuleUncheckedUpdateManyInput>
    /**
     * Filter which Rules to update
     */
    where?: RuleWhereInput
    /**
     * Limit how many Rules to update.
     */
    limit?: number
  }

  /**
   * Rule updateManyAndReturn
   */
  export type RuleUpdateManyAndReturnArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Rule
     */
    select?: RuleSelectUpdateManyAndReturn<ExtArgs> | null
    /**
     * Omit specific fields from the Rule
     */
    omit?: RuleOmit<ExtArgs> | null
    /**
     * The data used to update Rules.
     */
    data: XOR<RuleUpdateManyMutationInput, RuleUncheckedUpdateManyInput>
    /**
     * Filter which Rules to update
     */
    where?: RuleWhereInput
    /**
     * Limit how many Rules to update.
     */
    limit?: number
  }

  /**
   * Rule upsert
   */
  export type RuleUpsertArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Rule
     */
    select?: RuleSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Rule
     */
    omit?: RuleOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleInclude<ExtArgs> | null
    /**
     * The filter to search for the Rule to update in case it exists.
     */
    where: RuleWhereUniqueInput
    /**
     * In case the Rule found by the `where` argument doesn't exist, create a new Rule with this data.
     */
    create: XOR<RuleCreateInput, RuleUncheckedCreateInput>
    /**
     * In case the Rule was found with the provided `where` argument, update it with this data.
     */
    update: XOR<RuleUpdateInput, RuleUncheckedUpdateInput>
  }

  /**
   * Rule delete
   */
  export type RuleDeleteArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Rule
     */
    select?: RuleSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Rule
     */
    omit?: RuleOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleInclude<ExtArgs> | null
    /**
     * Filter which Rule to delete.
     */
    where: RuleWhereUniqueInput
  }

  /**
   * Rule deleteMany
   */
  export type RuleDeleteManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Filter which Rules to delete
     */
    where?: RuleWhereInput
    /**
     * Limit how many Rules to delete.
     */
    limit?: number
  }

  /**
   * Rule.conditions
   */
  export type Rule$conditionsArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the RuleCondition
     */
    select?: RuleConditionSelect<ExtArgs> | null
    /**
     * Omit specific fields from the RuleCondition
     */
    omit?: RuleConditionOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleConditionInclude<ExtArgs> | null
    where?: RuleConditionWhereInput
    orderBy?: RuleConditionOrderByWithRelationInput | RuleConditionOrderByWithRelationInput[]
    cursor?: RuleConditionWhereUniqueInput
    take?: number
    skip?: number
    distinct?: RuleConditionScalarFieldEnum | RuleConditionScalarFieldEnum[]
  }

  /**
   * Rule without action
   */
  export type RuleDefaultArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the Rule
     */
    select?: RuleSelect<ExtArgs> | null
    /**
     * Omit specific fields from the Rule
     */
    omit?: RuleOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleInclude<ExtArgs> | null
  }


  /**
   * Model RuleCondition
   */

  export type AggregateRuleCondition = {
    _count: RuleConditionCountAggregateOutputType | null
    _avg: RuleConditionAvgAggregateOutputType | null
    _sum: RuleConditionSumAggregateOutputType | null
    _min: RuleConditionMinAggregateOutputType | null
    _max: RuleConditionMaxAggregateOutputType | null
  }

  export type RuleConditionAvgAggregateOutputType = {
    id: number | null
    ruleId: number | null
  }

  export type RuleConditionSumAggregateOutputType = {
    id: number | null
    ruleId: number | null
  }

  export type RuleConditionMinAggregateOutputType = {
    id: number | null
    field: string | null
    operator: $Enums.ConditionOperator | null
    value: string | null
    ruleId: number | null
  }

  export type RuleConditionMaxAggregateOutputType = {
    id: number | null
    field: string | null
    operator: $Enums.ConditionOperator | null
    value: string | null
    ruleId: number | null
  }

  export type RuleConditionCountAggregateOutputType = {
    id: number
    field: number
    operator: number
    value: number
    ruleId: number
    _all: number
  }


  export type RuleConditionAvgAggregateInputType = {
    id?: true
    ruleId?: true
  }

  export type RuleConditionSumAggregateInputType = {
    id?: true
    ruleId?: true
  }

  export type RuleConditionMinAggregateInputType = {
    id?: true
    field?: true
    operator?: true
    value?: true
    ruleId?: true
  }

  export type RuleConditionMaxAggregateInputType = {
    id?: true
    field?: true
    operator?: true
    value?: true
    ruleId?: true
  }

  export type RuleConditionCountAggregateInputType = {
    id?: true
    field?: true
    operator?: true
    value?: true
    ruleId?: true
    _all?: true
  }

  export type RuleConditionAggregateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Filter which RuleCondition to aggregate.
     */
    where?: RuleConditionWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of RuleConditions to fetch.
     */
    orderBy?: RuleConditionOrderByWithRelationInput | RuleConditionOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the start position
     */
    cursor?: RuleConditionWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` RuleConditions from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` RuleConditions.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Count returned RuleConditions
    **/
    _count?: true | RuleConditionCountAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to average
    **/
    _avg?: RuleConditionAvgAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to sum
    **/
    _sum?: RuleConditionSumAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to find the minimum value
    **/
    _min?: RuleConditionMinAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to find the maximum value
    **/
    _max?: RuleConditionMaxAggregateInputType
  }

  export type GetRuleConditionAggregateType<T extends RuleConditionAggregateArgs> = {
        [P in keyof T & keyof AggregateRuleCondition]: P extends '_count' | 'count'
      ? T[P] extends true
        ? number
        : GetScalarType<T[P], AggregateRuleCondition[P]>
      : GetScalarType<T[P], AggregateRuleCondition[P]>
  }




  export type RuleConditionGroupByArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    where?: RuleConditionWhereInput
    orderBy?: RuleConditionOrderByWithAggregationInput | RuleConditionOrderByWithAggregationInput[]
    by: RuleConditionScalarFieldEnum[] | RuleConditionScalarFieldEnum
    having?: RuleConditionScalarWhereWithAggregatesInput
    take?: number
    skip?: number
    _count?: RuleConditionCountAggregateInputType | true
    _avg?: RuleConditionAvgAggregateInputType
    _sum?: RuleConditionSumAggregateInputType
    _min?: RuleConditionMinAggregateInputType
    _max?: RuleConditionMaxAggregateInputType
  }

  export type RuleConditionGroupByOutputType = {
    id: number
    field: string
    operator: $Enums.ConditionOperator
    value: string
    ruleId: number
    _count: RuleConditionCountAggregateOutputType | null
    _avg: RuleConditionAvgAggregateOutputType | null
    _sum: RuleConditionSumAggregateOutputType | null
    _min: RuleConditionMinAggregateOutputType | null
    _max: RuleConditionMaxAggregateOutputType | null
  }

  type GetRuleConditionGroupByPayload<T extends RuleConditionGroupByArgs> = Prisma.PrismaPromise<
    Array<
      PickEnumerable<RuleConditionGroupByOutputType, T['by']> &
        {
          [P in ((keyof T) & (keyof RuleConditionGroupByOutputType))]: P extends '_count'
            ? T[P] extends boolean
              ? number
              : GetScalarType<T[P], RuleConditionGroupByOutputType[P]>
            : GetScalarType<T[P], RuleConditionGroupByOutputType[P]>
        }
      >
    >


  export type RuleConditionSelect<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    field?: boolean
    operator?: boolean
    value?: boolean
    ruleId?: boolean
    rule?: boolean | RuleDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["ruleCondition"]>

  export type RuleConditionSelectCreateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    field?: boolean
    operator?: boolean
    value?: boolean
    ruleId?: boolean
    rule?: boolean | RuleDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["ruleCondition"]>

  export type RuleConditionSelectUpdateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    field?: boolean
    operator?: boolean
    value?: boolean
    ruleId?: boolean
    rule?: boolean | RuleDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["ruleCondition"]>

  export type RuleConditionSelectScalar = {
    id?: boolean
    field?: boolean
    operator?: boolean
    value?: boolean
    ruleId?: boolean
  }

  export type RuleConditionOmit<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetOmit<"id" | "field" | "operator" | "value" | "ruleId", ExtArgs["result"]["ruleCondition"]>
  export type RuleConditionInclude<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    rule?: boolean | RuleDefaultArgs<ExtArgs>
  }
  export type RuleConditionIncludeCreateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    rule?: boolean | RuleDefaultArgs<ExtArgs>
  }
  export type RuleConditionIncludeUpdateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    rule?: boolean | RuleDefaultArgs<ExtArgs>
  }

  export type $RuleConditionPayload<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    name: "RuleCondition"
    objects: {
      rule: Prisma.$RulePayload<ExtArgs>
    }
    scalars: $Extensions.GetPayloadResult<{
      id: number
      field: string
      operator: $Enums.ConditionOperator
      value: string
      ruleId: number
    }, ExtArgs["result"]["ruleCondition"]>
    composites: {}
  }

  type RuleConditionGetPayload<S extends boolean | null | undefined | RuleConditionDefaultArgs> = $Result.GetResult<Prisma.$RuleConditionPayload, S>

  type RuleConditionCountArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> =
    Omit<RuleConditionFindManyArgs, 'select' | 'include' | 'distinct' | 'omit'> & {
      select?: RuleConditionCountAggregateInputType | true
    }

  export interface RuleConditionDelegate<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs, GlobalOmitOptions = {}> {
    [K: symbol]: { types: Prisma.TypeMap<ExtArgs>['model']['RuleCondition'], meta: { name: 'RuleCondition' } }
    /**
     * Find zero or one RuleCondition that matches the filter.
     * @param {RuleConditionFindUniqueArgs} args - Arguments to find a RuleCondition
     * @example
     * // Get one RuleCondition
     * const ruleCondition = await prisma.ruleCondition.findUnique({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findUnique<T extends RuleConditionFindUniqueArgs>(args: SelectSubset<T, RuleConditionFindUniqueArgs<ExtArgs>>): Prisma__RuleConditionClient<$Result.GetResult<Prisma.$RuleConditionPayload<ExtArgs>, T, "findUnique", GlobalOmitOptions> | null, null, ExtArgs, GlobalOmitOptions>

    /**
     * Find one RuleCondition that matches the filter or throw an error with `error.code='P2025'`
     * if no matches were found.
     * @param {RuleConditionFindUniqueOrThrowArgs} args - Arguments to find a RuleCondition
     * @example
     * // Get one RuleCondition
     * const ruleCondition = await prisma.ruleCondition.findUniqueOrThrow({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findUniqueOrThrow<T extends RuleConditionFindUniqueOrThrowArgs>(args: SelectSubset<T, RuleConditionFindUniqueOrThrowArgs<ExtArgs>>): Prisma__RuleConditionClient<$Result.GetResult<Prisma.$RuleConditionPayload<ExtArgs>, T, "findUniqueOrThrow", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Find the first RuleCondition that matches the filter.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {RuleConditionFindFirstArgs} args - Arguments to find a RuleCondition
     * @example
     * // Get one RuleCondition
     * const ruleCondition = await prisma.ruleCondition.findFirst({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findFirst<T extends RuleConditionFindFirstArgs>(args?: SelectSubset<T, RuleConditionFindFirstArgs<ExtArgs>>): Prisma__RuleConditionClient<$Result.GetResult<Prisma.$RuleConditionPayload<ExtArgs>, T, "findFirst", GlobalOmitOptions> | null, null, ExtArgs, GlobalOmitOptions>

    /**
     * Find the first RuleCondition that matches the filter or
     * throw `PrismaKnownClientError` with `P2025` code if no matches were found.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {RuleConditionFindFirstOrThrowArgs} args - Arguments to find a RuleCondition
     * @example
     * // Get one RuleCondition
     * const ruleCondition = await prisma.ruleCondition.findFirstOrThrow({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findFirstOrThrow<T extends RuleConditionFindFirstOrThrowArgs>(args?: SelectSubset<T, RuleConditionFindFirstOrThrowArgs<ExtArgs>>): Prisma__RuleConditionClient<$Result.GetResult<Prisma.$RuleConditionPayload<ExtArgs>, T, "findFirstOrThrow", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Find zero or more RuleConditions that matches the filter.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {RuleConditionFindManyArgs} args - Arguments to filter and select certain fields only.
     * @example
     * // Get all RuleConditions
     * const ruleConditions = await prisma.ruleCondition.findMany()
     * 
     * // Get first 10 RuleConditions
     * const ruleConditions = await prisma.ruleCondition.findMany({ take: 10 })
     * 
     * // Only select the `id`
     * const ruleConditionWithIdOnly = await prisma.ruleCondition.findMany({ select: { id: true } })
     * 
     */
    findMany<T extends RuleConditionFindManyArgs>(args?: SelectSubset<T, RuleConditionFindManyArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$RuleConditionPayload<ExtArgs>, T, "findMany", GlobalOmitOptions>>

    /**
     * Create a RuleCondition.
     * @param {RuleConditionCreateArgs} args - Arguments to create a RuleCondition.
     * @example
     * // Create one RuleCondition
     * const RuleCondition = await prisma.ruleCondition.create({
     *   data: {
     *     // ... data to create a RuleCondition
     *   }
     * })
     * 
     */
    create<T extends RuleConditionCreateArgs>(args: SelectSubset<T, RuleConditionCreateArgs<ExtArgs>>): Prisma__RuleConditionClient<$Result.GetResult<Prisma.$RuleConditionPayload<ExtArgs>, T, "create", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Create many RuleConditions.
     * @param {RuleConditionCreateManyArgs} args - Arguments to create many RuleConditions.
     * @example
     * // Create many RuleConditions
     * const ruleCondition = await prisma.ruleCondition.createMany({
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     *     
     */
    createMany<T extends RuleConditionCreateManyArgs>(args?: SelectSubset<T, RuleConditionCreateManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Create many RuleConditions and returns the data saved in the database.
     * @param {RuleConditionCreateManyAndReturnArgs} args - Arguments to create many RuleConditions.
     * @example
     * // Create many RuleConditions
     * const ruleCondition = await prisma.ruleCondition.createManyAndReturn({
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * 
     * // Create many RuleConditions and only return the `id`
     * const ruleConditionWithIdOnly = await prisma.ruleCondition.createManyAndReturn({
     *   select: { id: true },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * 
     */
    createManyAndReturn<T extends RuleConditionCreateManyAndReturnArgs>(args?: SelectSubset<T, RuleConditionCreateManyAndReturnArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$RuleConditionPayload<ExtArgs>, T, "createManyAndReturn", GlobalOmitOptions>>

    /**
     * Delete a RuleCondition.
     * @param {RuleConditionDeleteArgs} args - Arguments to delete one RuleCondition.
     * @example
     * // Delete one RuleCondition
     * const RuleCondition = await prisma.ruleCondition.delete({
     *   where: {
     *     // ... filter to delete one RuleCondition
     *   }
     * })
     * 
     */
    delete<T extends RuleConditionDeleteArgs>(args: SelectSubset<T, RuleConditionDeleteArgs<ExtArgs>>): Prisma__RuleConditionClient<$Result.GetResult<Prisma.$RuleConditionPayload<ExtArgs>, T, "delete", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Update one RuleCondition.
     * @param {RuleConditionUpdateArgs} args - Arguments to update one RuleCondition.
     * @example
     * // Update one RuleCondition
     * const ruleCondition = await prisma.ruleCondition.update({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: {
     *     // ... provide data here
     *   }
     * })
     * 
     */
    update<T extends RuleConditionUpdateArgs>(args: SelectSubset<T, RuleConditionUpdateArgs<ExtArgs>>): Prisma__RuleConditionClient<$Result.GetResult<Prisma.$RuleConditionPayload<ExtArgs>, T, "update", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Delete zero or more RuleConditions.
     * @param {RuleConditionDeleteManyArgs} args - Arguments to filter RuleConditions to delete.
     * @example
     * // Delete a few RuleConditions
     * const { count } = await prisma.ruleCondition.deleteMany({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     * 
     */
    deleteMany<T extends RuleConditionDeleteManyArgs>(args?: SelectSubset<T, RuleConditionDeleteManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Update zero or more RuleConditions.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {RuleConditionUpdateManyArgs} args - Arguments to update one or more rows.
     * @example
     * // Update many RuleConditions
     * const ruleCondition = await prisma.ruleCondition.updateMany({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: {
     *     // ... provide data here
     *   }
     * })
     * 
     */
    updateMany<T extends RuleConditionUpdateManyArgs>(args: SelectSubset<T, RuleConditionUpdateManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Update zero or more RuleConditions and returns the data updated in the database.
     * @param {RuleConditionUpdateManyAndReturnArgs} args - Arguments to update many RuleConditions.
     * @example
     * // Update many RuleConditions
     * const ruleCondition = await prisma.ruleCondition.updateManyAndReturn({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * 
     * // Update zero or more RuleConditions and only return the `id`
     * const ruleConditionWithIdOnly = await prisma.ruleCondition.updateManyAndReturn({
     *   select: { id: true },
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * 
     */
    updateManyAndReturn<T extends RuleConditionUpdateManyAndReturnArgs>(args: SelectSubset<T, RuleConditionUpdateManyAndReturnArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$RuleConditionPayload<ExtArgs>, T, "updateManyAndReturn", GlobalOmitOptions>>

    /**
     * Create or update one RuleCondition.
     * @param {RuleConditionUpsertArgs} args - Arguments to update or create a RuleCondition.
     * @example
     * // Update or create a RuleCondition
     * const ruleCondition = await prisma.ruleCondition.upsert({
     *   create: {
     *     // ... data to create a RuleCondition
     *   },
     *   update: {
     *     // ... in case it already exists, update
     *   },
     *   where: {
     *     // ... the filter for the RuleCondition we want to update
     *   }
     * })
     */
    upsert<T extends RuleConditionUpsertArgs>(args: SelectSubset<T, RuleConditionUpsertArgs<ExtArgs>>): Prisma__RuleConditionClient<$Result.GetResult<Prisma.$RuleConditionPayload<ExtArgs>, T, "upsert", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>


    /**
     * Count the number of RuleConditions.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {RuleConditionCountArgs} args - Arguments to filter RuleConditions to count.
     * @example
     * // Count the number of RuleConditions
     * const count = await prisma.ruleCondition.count({
     *   where: {
     *     // ... the filter for the RuleConditions we want to count
     *   }
     * })
    **/
    count<T extends RuleConditionCountArgs>(
      args?: Subset<T, RuleConditionCountArgs>,
    ): Prisma.PrismaPromise<
      T extends $Utils.Record<'select', any>
        ? T['select'] extends true
          ? number
          : GetScalarType<T['select'], RuleConditionCountAggregateOutputType>
        : number
    >

    /**
     * Allows you to perform aggregations operations on a RuleCondition.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {RuleConditionAggregateArgs} args - Select which aggregations you would like to apply and on what fields.
     * @example
     * // Ordered by age ascending
     * // Where email contains prisma.io
     * // Limited to the 10 users
     * const aggregations = await prisma.user.aggregate({
     *   _avg: {
     *     age: true,
     *   },
     *   where: {
     *     email: {
     *       contains: "prisma.io",
     *     },
     *   },
     *   orderBy: {
     *     age: "asc",
     *   },
     *   take: 10,
     * })
    **/
    aggregate<T extends RuleConditionAggregateArgs>(args: Subset<T, RuleConditionAggregateArgs>): Prisma.PrismaPromise<GetRuleConditionAggregateType<T>>

    /**
     * Group by RuleCondition.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {RuleConditionGroupByArgs} args - Group by arguments.
     * @example
     * // Group by city, order by createdAt, get count
     * const result = await prisma.user.groupBy({
     *   by: ['city', 'createdAt'],
     *   orderBy: {
     *     createdAt: true
     *   },
     *   _count: {
     *     _all: true
     *   },
     * })
     * 
    **/
    groupBy<
      T extends RuleConditionGroupByArgs,
      HasSelectOrTake extends Or<
        Extends<'skip', Keys<T>>,
        Extends<'take', Keys<T>>
      >,
      OrderByArg extends True extends HasSelectOrTake
        ? { orderBy: RuleConditionGroupByArgs['orderBy'] }
        : { orderBy?: RuleConditionGroupByArgs['orderBy'] },
      OrderFields extends ExcludeUnderscoreKeys<Keys<MaybeTupleToUnion<T['orderBy']>>>,
      ByFields extends MaybeTupleToUnion<T['by']>,
      ByValid extends Has<ByFields, OrderFields>,
      HavingFields extends GetHavingFields<T['having']>,
      HavingValid extends Has<ByFields, HavingFields>,
      ByEmpty extends T['by'] extends never[] ? True : False,
      InputErrors extends ByEmpty extends True
      ? `Error: "by" must not be empty.`
      : HavingValid extends False
      ? {
          [P in HavingFields]: P extends ByFields
            ? never
            : P extends string
            ? `Error: Field "${P}" used in "having" needs to be provided in "by".`
            : [
                Error,
                'Field ',
                P,
                ` in "having" needs to be provided in "by"`,
              ]
        }[HavingFields]
      : 'take' extends Keys<T>
      ? 'orderBy' extends Keys<T>
        ? ByValid extends True
          ? {}
          : {
              [P in OrderFields]: P extends ByFields
                ? never
                : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
            }[OrderFields]
        : 'Error: If you provide "take", you also need to provide "orderBy"'
      : 'skip' extends Keys<T>
      ? 'orderBy' extends Keys<T>
        ? ByValid extends True
          ? {}
          : {
              [P in OrderFields]: P extends ByFields
                ? never
                : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
            }[OrderFields]
        : 'Error: If you provide "skip", you also need to provide "orderBy"'
      : ByValid extends True
      ? {}
      : {
          [P in OrderFields]: P extends ByFields
            ? never
            : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
        }[OrderFields]
    >(args: SubsetIntersection<T, RuleConditionGroupByArgs, OrderByArg> & InputErrors): {} extends InputErrors ? GetRuleConditionGroupByPayload<T> : Prisma.PrismaPromise<InputErrors>
  /**
   * Fields of the RuleCondition model
   */
  readonly fields: RuleConditionFieldRefs;
  }

  /**
   * The delegate class that acts as a "Promise-like" for RuleCondition.
   * Why is this prefixed with `Prisma__`?
   * Because we want to prevent naming conflicts as mentioned in
   * https://github.com/prisma/prisma-client-js/issues/707
   */
  export interface Prisma__RuleConditionClient<T, Null = never, ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs, GlobalOmitOptions = {}> extends Prisma.PrismaPromise<T> {
    readonly [Symbol.toStringTag]: "PrismaPromise"
    rule<T extends RuleDefaultArgs<ExtArgs> = {}>(args?: Subset<T, RuleDefaultArgs<ExtArgs>>): Prisma__RuleClient<$Result.GetResult<Prisma.$RulePayload<ExtArgs>, T, "findUniqueOrThrow", GlobalOmitOptions> | Null, Null, ExtArgs, GlobalOmitOptions>
    /**
     * Attaches callbacks for the resolution and/or rejection of the Promise.
     * @param onfulfilled The callback to execute when the Promise is resolved.
     * @param onrejected The callback to execute when the Promise is rejected.
     * @returns A Promise for the completion of which ever callback is executed.
     */
    then<TResult1 = T, TResult2 = never>(onfulfilled?: ((value: T) => TResult1 | PromiseLike<TResult1>) | undefined | null, onrejected?: ((reason: any) => TResult2 | PromiseLike<TResult2>) | undefined | null): $Utils.JsPromise<TResult1 | TResult2>
    /**
     * Attaches a callback for only the rejection of the Promise.
     * @param onrejected The callback to execute when the Promise is rejected.
     * @returns A Promise for the completion of the callback.
     */
    catch<TResult = never>(onrejected?: ((reason: any) => TResult | PromiseLike<TResult>) | undefined | null): $Utils.JsPromise<T | TResult>
    /**
     * Attaches a callback that is invoked when the Promise is settled (fulfilled or rejected). The
     * resolved value cannot be modified from the callback.
     * @param onfinally The callback to execute when the Promise is settled (fulfilled or rejected).
     * @returns A Promise for the completion of the callback.
     */
    finally(onfinally?: (() => void) | undefined | null): $Utils.JsPromise<T>
  }




  /**
   * Fields of the RuleCondition model
   */
  interface RuleConditionFieldRefs {
    readonly id: FieldRef<"RuleCondition", 'Int'>
    readonly field: FieldRef<"RuleCondition", 'String'>
    readonly operator: FieldRef<"RuleCondition", 'ConditionOperator'>
    readonly value: FieldRef<"RuleCondition", 'String'>
    readonly ruleId: FieldRef<"RuleCondition", 'Int'>
  }
    

  // Custom InputTypes
  /**
   * RuleCondition findUnique
   */
  export type RuleConditionFindUniqueArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the RuleCondition
     */
    select?: RuleConditionSelect<ExtArgs> | null
    /**
     * Omit specific fields from the RuleCondition
     */
    omit?: RuleConditionOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleConditionInclude<ExtArgs> | null
    /**
     * Filter, which RuleCondition to fetch.
     */
    where: RuleConditionWhereUniqueInput
  }

  /**
   * RuleCondition findUniqueOrThrow
   */
  export type RuleConditionFindUniqueOrThrowArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the RuleCondition
     */
    select?: RuleConditionSelect<ExtArgs> | null
    /**
     * Omit specific fields from the RuleCondition
     */
    omit?: RuleConditionOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleConditionInclude<ExtArgs> | null
    /**
     * Filter, which RuleCondition to fetch.
     */
    where: RuleConditionWhereUniqueInput
  }

  /**
   * RuleCondition findFirst
   */
  export type RuleConditionFindFirstArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the RuleCondition
     */
    select?: RuleConditionSelect<ExtArgs> | null
    /**
     * Omit specific fields from the RuleCondition
     */
    omit?: RuleConditionOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleConditionInclude<ExtArgs> | null
    /**
     * Filter, which RuleCondition to fetch.
     */
    where?: RuleConditionWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of RuleConditions to fetch.
     */
    orderBy?: RuleConditionOrderByWithRelationInput | RuleConditionOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for searching for RuleConditions.
     */
    cursor?: RuleConditionWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` RuleConditions from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` RuleConditions.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/distinct Distinct Docs}
     * 
     * Filter by unique combinations of RuleConditions.
     */
    distinct?: RuleConditionScalarFieldEnum | RuleConditionScalarFieldEnum[]
  }

  /**
   * RuleCondition findFirstOrThrow
   */
  export type RuleConditionFindFirstOrThrowArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the RuleCondition
     */
    select?: RuleConditionSelect<ExtArgs> | null
    /**
     * Omit specific fields from the RuleCondition
     */
    omit?: RuleConditionOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleConditionInclude<ExtArgs> | null
    /**
     * Filter, which RuleCondition to fetch.
     */
    where?: RuleConditionWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of RuleConditions to fetch.
     */
    orderBy?: RuleConditionOrderByWithRelationInput | RuleConditionOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for searching for RuleConditions.
     */
    cursor?: RuleConditionWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` RuleConditions from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` RuleConditions.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/distinct Distinct Docs}
     * 
     * Filter by unique combinations of RuleConditions.
     */
    distinct?: RuleConditionScalarFieldEnum | RuleConditionScalarFieldEnum[]
  }

  /**
   * RuleCondition findMany
   */
  export type RuleConditionFindManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the RuleCondition
     */
    select?: RuleConditionSelect<ExtArgs> | null
    /**
     * Omit specific fields from the RuleCondition
     */
    omit?: RuleConditionOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleConditionInclude<ExtArgs> | null
    /**
     * Filter, which RuleConditions to fetch.
     */
    where?: RuleConditionWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of RuleConditions to fetch.
     */
    orderBy?: RuleConditionOrderByWithRelationInput | RuleConditionOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for listing RuleConditions.
     */
    cursor?: RuleConditionWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` RuleConditions from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` RuleConditions.
     */
    skip?: number
    distinct?: RuleConditionScalarFieldEnum | RuleConditionScalarFieldEnum[]
  }

  /**
   * RuleCondition create
   */
  export type RuleConditionCreateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the RuleCondition
     */
    select?: RuleConditionSelect<ExtArgs> | null
    /**
     * Omit specific fields from the RuleCondition
     */
    omit?: RuleConditionOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleConditionInclude<ExtArgs> | null
    /**
     * The data needed to create a RuleCondition.
     */
    data: XOR<RuleConditionCreateInput, RuleConditionUncheckedCreateInput>
  }

  /**
   * RuleCondition createMany
   */
  export type RuleConditionCreateManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * The data used to create many RuleConditions.
     */
    data: RuleConditionCreateManyInput | RuleConditionCreateManyInput[]
    skipDuplicates?: boolean
  }

  /**
   * RuleCondition createManyAndReturn
   */
  export type RuleConditionCreateManyAndReturnArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the RuleCondition
     */
    select?: RuleConditionSelectCreateManyAndReturn<ExtArgs> | null
    /**
     * Omit specific fields from the RuleCondition
     */
    omit?: RuleConditionOmit<ExtArgs> | null
    /**
     * The data used to create many RuleConditions.
     */
    data: RuleConditionCreateManyInput | RuleConditionCreateManyInput[]
    skipDuplicates?: boolean
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleConditionIncludeCreateManyAndReturn<ExtArgs> | null
  }

  /**
   * RuleCondition update
   */
  export type RuleConditionUpdateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the RuleCondition
     */
    select?: RuleConditionSelect<ExtArgs> | null
    /**
     * Omit specific fields from the RuleCondition
     */
    omit?: RuleConditionOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleConditionInclude<ExtArgs> | null
    /**
     * The data needed to update a RuleCondition.
     */
    data: XOR<RuleConditionUpdateInput, RuleConditionUncheckedUpdateInput>
    /**
     * Choose, which RuleCondition to update.
     */
    where: RuleConditionWhereUniqueInput
  }

  /**
   * RuleCondition updateMany
   */
  export type RuleConditionUpdateManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * The data used to update RuleConditions.
     */
    data: XOR<RuleConditionUpdateManyMutationInput, RuleConditionUncheckedUpdateManyInput>
    /**
     * Filter which RuleConditions to update
     */
    where?: RuleConditionWhereInput
    /**
     * Limit how many RuleConditions to update.
     */
    limit?: number
  }

  /**
   * RuleCondition updateManyAndReturn
   */
  export type RuleConditionUpdateManyAndReturnArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the RuleCondition
     */
    select?: RuleConditionSelectUpdateManyAndReturn<ExtArgs> | null
    /**
     * Omit specific fields from the RuleCondition
     */
    omit?: RuleConditionOmit<ExtArgs> | null
    /**
     * The data used to update RuleConditions.
     */
    data: XOR<RuleConditionUpdateManyMutationInput, RuleConditionUncheckedUpdateManyInput>
    /**
     * Filter which RuleConditions to update
     */
    where?: RuleConditionWhereInput
    /**
     * Limit how many RuleConditions to update.
     */
    limit?: number
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleConditionIncludeUpdateManyAndReturn<ExtArgs> | null
  }

  /**
   * RuleCondition upsert
   */
  export type RuleConditionUpsertArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the RuleCondition
     */
    select?: RuleConditionSelect<ExtArgs> | null
    /**
     * Omit specific fields from the RuleCondition
     */
    omit?: RuleConditionOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleConditionInclude<ExtArgs> | null
    /**
     * The filter to search for the RuleCondition to update in case it exists.
     */
    where: RuleConditionWhereUniqueInput
    /**
     * In case the RuleCondition found by the `where` argument doesn't exist, create a new RuleCondition with this data.
     */
    create: XOR<RuleConditionCreateInput, RuleConditionUncheckedCreateInput>
    /**
     * In case the RuleCondition was found with the provided `where` argument, update it with this data.
     */
    update: XOR<RuleConditionUpdateInput, RuleConditionUncheckedUpdateInput>
  }

  /**
   * RuleCondition delete
   */
  export type RuleConditionDeleteArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the RuleCondition
     */
    select?: RuleConditionSelect<ExtArgs> | null
    /**
     * Omit specific fields from the RuleCondition
     */
    omit?: RuleConditionOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleConditionInclude<ExtArgs> | null
    /**
     * Filter which RuleCondition to delete.
     */
    where: RuleConditionWhereUniqueInput
  }

  /**
   * RuleCondition deleteMany
   */
  export type RuleConditionDeleteManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Filter which RuleConditions to delete
     */
    where?: RuleConditionWhereInput
    /**
     * Limit how many RuleConditions to delete.
     */
    limit?: number
  }

  /**
   * RuleCondition without action
   */
  export type RuleConditionDefaultArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the RuleCondition
     */
    select?: RuleConditionSelect<ExtArgs> | null
    /**
     * Omit specific fields from the RuleCondition
     */
    omit?: RuleConditionOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: RuleConditionInclude<ExtArgs> | null
  }


  /**
   * Model UserClient
   */

  export type AggregateUserClient = {
    _count: UserClientCountAggregateOutputType | null
    _avg: UserClientAvgAggregateOutputType | null
    _sum: UserClientSumAggregateOutputType | null
    _min: UserClientMinAggregateOutputType | null
    _max: UserClientMaxAggregateOutputType | null
  }

  export type UserClientAvgAggregateOutputType = {
    id: number | null
    userId: number | null
    clientId: number | null
  }

  export type UserClientSumAggregateOutputType = {
    id: number | null
    userId: number | null
    clientId: number | null
  }

  export type UserClientMinAggregateOutputType = {
    id: number | null
    userId: number | null
    clientId: number | null
  }

  export type UserClientMaxAggregateOutputType = {
    id: number | null
    userId: number | null
    clientId: number | null
  }

  export type UserClientCountAggregateOutputType = {
    id: number
    userId: number
    clientId: number
    _all: number
  }


  export type UserClientAvgAggregateInputType = {
    id?: true
    userId?: true
    clientId?: true
  }

  export type UserClientSumAggregateInputType = {
    id?: true
    userId?: true
    clientId?: true
  }

  export type UserClientMinAggregateInputType = {
    id?: true
    userId?: true
    clientId?: true
  }

  export type UserClientMaxAggregateInputType = {
    id?: true
    userId?: true
    clientId?: true
  }

  export type UserClientCountAggregateInputType = {
    id?: true
    userId?: true
    clientId?: true
    _all?: true
  }

  export type UserClientAggregateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Filter which UserClient to aggregate.
     */
    where?: UserClientWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of UserClients to fetch.
     */
    orderBy?: UserClientOrderByWithRelationInput | UserClientOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the start position
     */
    cursor?: UserClientWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` UserClients from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` UserClients.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Count returned UserClients
    **/
    _count?: true | UserClientCountAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to average
    **/
    _avg?: UserClientAvgAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to sum
    **/
    _sum?: UserClientSumAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to find the minimum value
    **/
    _min?: UserClientMinAggregateInputType
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/aggregations Aggregation Docs}
     * 
     * Select which fields to find the maximum value
    **/
    _max?: UserClientMaxAggregateInputType
  }

  export type GetUserClientAggregateType<T extends UserClientAggregateArgs> = {
        [P in keyof T & keyof AggregateUserClient]: P extends '_count' | 'count'
      ? T[P] extends true
        ? number
        : GetScalarType<T[P], AggregateUserClient[P]>
      : GetScalarType<T[P], AggregateUserClient[P]>
  }




  export type UserClientGroupByArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    where?: UserClientWhereInput
    orderBy?: UserClientOrderByWithAggregationInput | UserClientOrderByWithAggregationInput[]
    by: UserClientScalarFieldEnum[] | UserClientScalarFieldEnum
    having?: UserClientScalarWhereWithAggregatesInput
    take?: number
    skip?: number
    _count?: UserClientCountAggregateInputType | true
    _avg?: UserClientAvgAggregateInputType
    _sum?: UserClientSumAggregateInputType
    _min?: UserClientMinAggregateInputType
    _max?: UserClientMaxAggregateInputType
  }

  export type UserClientGroupByOutputType = {
    id: number
    userId: number
    clientId: number
    _count: UserClientCountAggregateOutputType | null
    _avg: UserClientAvgAggregateOutputType | null
    _sum: UserClientSumAggregateOutputType | null
    _min: UserClientMinAggregateOutputType | null
    _max: UserClientMaxAggregateOutputType | null
  }

  type GetUserClientGroupByPayload<T extends UserClientGroupByArgs> = Prisma.PrismaPromise<
    Array<
      PickEnumerable<UserClientGroupByOutputType, T['by']> &
        {
          [P in ((keyof T) & (keyof UserClientGroupByOutputType))]: P extends '_count'
            ? T[P] extends boolean
              ? number
              : GetScalarType<T[P], UserClientGroupByOutputType[P]>
            : GetScalarType<T[P], UserClientGroupByOutputType[P]>
        }
      >
    >


  export type UserClientSelect<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    userId?: boolean
    clientId?: boolean
    user?: boolean | UserDefaultArgs<ExtArgs>
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["userClient"]>

  export type UserClientSelectCreateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    userId?: boolean
    clientId?: boolean
    user?: boolean | UserDefaultArgs<ExtArgs>
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["userClient"]>

  export type UserClientSelectUpdateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetSelect<{
    id?: boolean
    userId?: boolean
    clientId?: boolean
    user?: boolean | UserDefaultArgs<ExtArgs>
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }, ExtArgs["result"]["userClient"]>

  export type UserClientSelectScalar = {
    id?: boolean
    userId?: boolean
    clientId?: boolean
  }

  export type UserClientOmit<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = $Extensions.GetOmit<"id" | "userId" | "clientId", ExtArgs["result"]["userClient"]>
  export type UserClientInclude<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    user?: boolean | UserDefaultArgs<ExtArgs>
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }
  export type UserClientIncludeCreateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    user?: boolean | UserDefaultArgs<ExtArgs>
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }
  export type UserClientIncludeUpdateManyAndReturn<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    user?: boolean | UserDefaultArgs<ExtArgs>
    client?: boolean | ClientDefaultArgs<ExtArgs>
  }

  export type $UserClientPayload<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    name: "UserClient"
    objects: {
      user: Prisma.$UserPayload<ExtArgs>
      client: Prisma.$ClientPayload<ExtArgs>
    }
    scalars: $Extensions.GetPayloadResult<{
      id: number
      userId: number
      clientId: number
    }, ExtArgs["result"]["userClient"]>
    composites: {}
  }

  type UserClientGetPayload<S extends boolean | null | undefined | UserClientDefaultArgs> = $Result.GetResult<Prisma.$UserClientPayload, S>

  type UserClientCountArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> =
    Omit<UserClientFindManyArgs, 'select' | 'include' | 'distinct' | 'omit'> & {
      select?: UserClientCountAggregateInputType | true
    }

  export interface UserClientDelegate<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs, GlobalOmitOptions = {}> {
    [K: symbol]: { types: Prisma.TypeMap<ExtArgs>['model']['UserClient'], meta: { name: 'UserClient' } }
    /**
     * Find zero or one UserClient that matches the filter.
     * @param {UserClientFindUniqueArgs} args - Arguments to find a UserClient
     * @example
     * // Get one UserClient
     * const userClient = await prisma.userClient.findUnique({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findUnique<T extends UserClientFindUniqueArgs>(args: SelectSubset<T, UserClientFindUniqueArgs<ExtArgs>>): Prisma__UserClientClient<$Result.GetResult<Prisma.$UserClientPayload<ExtArgs>, T, "findUnique", GlobalOmitOptions> | null, null, ExtArgs, GlobalOmitOptions>

    /**
     * Find one UserClient that matches the filter or throw an error with `error.code='P2025'`
     * if no matches were found.
     * @param {UserClientFindUniqueOrThrowArgs} args - Arguments to find a UserClient
     * @example
     * // Get one UserClient
     * const userClient = await prisma.userClient.findUniqueOrThrow({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findUniqueOrThrow<T extends UserClientFindUniqueOrThrowArgs>(args: SelectSubset<T, UserClientFindUniqueOrThrowArgs<ExtArgs>>): Prisma__UserClientClient<$Result.GetResult<Prisma.$UserClientPayload<ExtArgs>, T, "findUniqueOrThrow", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Find the first UserClient that matches the filter.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {UserClientFindFirstArgs} args - Arguments to find a UserClient
     * @example
     * // Get one UserClient
     * const userClient = await prisma.userClient.findFirst({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findFirst<T extends UserClientFindFirstArgs>(args?: SelectSubset<T, UserClientFindFirstArgs<ExtArgs>>): Prisma__UserClientClient<$Result.GetResult<Prisma.$UserClientPayload<ExtArgs>, T, "findFirst", GlobalOmitOptions> | null, null, ExtArgs, GlobalOmitOptions>

    /**
     * Find the first UserClient that matches the filter or
     * throw `PrismaKnownClientError` with `P2025` code if no matches were found.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {UserClientFindFirstOrThrowArgs} args - Arguments to find a UserClient
     * @example
     * // Get one UserClient
     * const userClient = await prisma.userClient.findFirstOrThrow({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     */
    findFirstOrThrow<T extends UserClientFindFirstOrThrowArgs>(args?: SelectSubset<T, UserClientFindFirstOrThrowArgs<ExtArgs>>): Prisma__UserClientClient<$Result.GetResult<Prisma.$UserClientPayload<ExtArgs>, T, "findFirstOrThrow", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Find zero or more UserClients that matches the filter.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {UserClientFindManyArgs} args - Arguments to filter and select certain fields only.
     * @example
     * // Get all UserClients
     * const userClients = await prisma.userClient.findMany()
     * 
     * // Get first 10 UserClients
     * const userClients = await prisma.userClient.findMany({ take: 10 })
     * 
     * // Only select the `id`
     * const userClientWithIdOnly = await prisma.userClient.findMany({ select: { id: true } })
     * 
     */
    findMany<T extends UserClientFindManyArgs>(args?: SelectSubset<T, UserClientFindManyArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$UserClientPayload<ExtArgs>, T, "findMany", GlobalOmitOptions>>

    /**
     * Create a UserClient.
     * @param {UserClientCreateArgs} args - Arguments to create a UserClient.
     * @example
     * // Create one UserClient
     * const UserClient = await prisma.userClient.create({
     *   data: {
     *     // ... data to create a UserClient
     *   }
     * })
     * 
     */
    create<T extends UserClientCreateArgs>(args: SelectSubset<T, UserClientCreateArgs<ExtArgs>>): Prisma__UserClientClient<$Result.GetResult<Prisma.$UserClientPayload<ExtArgs>, T, "create", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Create many UserClients.
     * @param {UserClientCreateManyArgs} args - Arguments to create many UserClients.
     * @example
     * // Create many UserClients
     * const userClient = await prisma.userClient.createMany({
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     *     
     */
    createMany<T extends UserClientCreateManyArgs>(args?: SelectSubset<T, UserClientCreateManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Create many UserClients and returns the data saved in the database.
     * @param {UserClientCreateManyAndReturnArgs} args - Arguments to create many UserClients.
     * @example
     * // Create many UserClients
     * const userClient = await prisma.userClient.createManyAndReturn({
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * 
     * // Create many UserClients and only return the `id`
     * const userClientWithIdOnly = await prisma.userClient.createManyAndReturn({
     *   select: { id: true },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * 
     */
    createManyAndReturn<T extends UserClientCreateManyAndReturnArgs>(args?: SelectSubset<T, UserClientCreateManyAndReturnArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$UserClientPayload<ExtArgs>, T, "createManyAndReturn", GlobalOmitOptions>>

    /**
     * Delete a UserClient.
     * @param {UserClientDeleteArgs} args - Arguments to delete one UserClient.
     * @example
     * // Delete one UserClient
     * const UserClient = await prisma.userClient.delete({
     *   where: {
     *     // ... filter to delete one UserClient
     *   }
     * })
     * 
     */
    delete<T extends UserClientDeleteArgs>(args: SelectSubset<T, UserClientDeleteArgs<ExtArgs>>): Prisma__UserClientClient<$Result.GetResult<Prisma.$UserClientPayload<ExtArgs>, T, "delete", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Update one UserClient.
     * @param {UserClientUpdateArgs} args - Arguments to update one UserClient.
     * @example
     * // Update one UserClient
     * const userClient = await prisma.userClient.update({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: {
     *     // ... provide data here
     *   }
     * })
     * 
     */
    update<T extends UserClientUpdateArgs>(args: SelectSubset<T, UserClientUpdateArgs<ExtArgs>>): Prisma__UserClientClient<$Result.GetResult<Prisma.$UserClientPayload<ExtArgs>, T, "update", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>

    /**
     * Delete zero or more UserClients.
     * @param {UserClientDeleteManyArgs} args - Arguments to filter UserClients to delete.
     * @example
     * // Delete a few UserClients
     * const { count } = await prisma.userClient.deleteMany({
     *   where: {
     *     // ... provide filter here
     *   }
     * })
     * 
     */
    deleteMany<T extends UserClientDeleteManyArgs>(args?: SelectSubset<T, UserClientDeleteManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Update zero or more UserClients.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {UserClientUpdateManyArgs} args - Arguments to update one or more rows.
     * @example
     * // Update many UserClients
     * const userClient = await prisma.userClient.updateMany({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: {
     *     // ... provide data here
     *   }
     * })
     * 
     */
    updateMany<T extends UserClientUpdateManyArgs>(args: SelectSubset<T, UserClientUpdateManyArgs<ExtArgs>>): Prisma.PrismaPromise<BatchPayload>

    /**
     * Update zero or more UserClients and returns the data updated in the database.
     * @param {UserClientUpdateManyAndReturnArgs} args - Arguments to update many UserClients.
     * @example
     * // Update many UserClients
     * const userClient = await prisma.userClient.updateManyAndReturn({
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * 
     * // Update zero or more UserClients and only return the `id`
     * const userClientWithIdOnly = await prisma.userClient.updateManyAndReturn({
     *   select: { id: true },
     *   where: {
     *     // ... provide filter here
     *   },
     *   data: [
     *     // ... provide data here
     *   ]
     * })
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * 
     */
    updateManyAndReturn<T extends UserClientUpdateManyAndReturnArgs>(args: SelectSubset<T, UserClientUpdateManyAndReturnArgs<ExtArgs>>): Prisma.PrismaPromise<$Result.GetResult<Prisma.$UserClientPayload<ExtArgs>, T, "updateManyAndReturn", GlobalOmitOptions>>

    /**
     * Create or update one UserClient.
     * @param {UserClientUpsertArgs} args - Arguments to update or create a UserClient.
     * @example
     * // Update or create a UserClient
     * const userClient = await prisma.userClient.upsert({
     *   create: {
     *     // ... data to create a UserClient
     *   },
     *   update: {
     *     // ... in case it already exists, update
     *   },
     *   where: {
     *     // ... the filter for the UserClient we want to update
     *   }
     * })
     */
    upsert<T extends UserClientUpsertArgs>(args: SelectSubset<T, UserClientUpsertArgs<ExtArgs>>): Prisma__UserClientClient<$Result.GetResult<Prisma.$UserClientPayload<ExtArgs>, T, "upsert", GlobalOmitOptions>, never, ExtArgs, GlobalOmitOptions>


    /**
     * Count the number of UserClients.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {UserClientCountArgs} args - Arguments to filter UserClients to count.
     * @example
     * // Count the number of UserClients
     * const count = await prisma.userClient.count({
     *   where: {
     *     // ... the filter for the UserClients we want to count
     *   }
     * })
    **/
    count<T extends UserClientCountArgs>(
      args?: Subset<T, UserClientCountArgs>,
    ): Prisma.PrismaPromise<
      T extends $Utils.Record<'select', any>
        ? T['select'] extends true
          ? number
          : GetScalarType<T['select'], UserClientCountAggregateOutputType>
        : number
    >

    /**
     * Allows you to perform aggregations operations on a UserClient.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {UserClientAggregateArgs} args - Select which aggregations you would like to apply and on what fields.
     * @example
     * // Ordered by age ascending
     * // Where email contains prisma.io
     * // Limited to the 10 users
     * const aggregations = await prisma.user.aggregate({
     *   _avg: {
     *     age: true,
     *   },
     *   where: {
     *     email: {
     *       contains: "prisma.io",
     *     },
     *   },
     *   orderBy: {
     *     age: "asc",
     *   },
     *   take: 10,
     * })
    **/
    aggregate<T extends UserClientAggregateArgs>(args: Subset<T, UserClientAggregateArgs>): Prisma.PrismaPromise<GetUserClientAggregateType<T>>

    /**
     * Group by UserClient.
     * Note, that providing `undefined` is treated as the value not being there.
     * Read more here: https://pris.ly/d/null-undefined
     * @param {UserClientGroupByArgs} args - Group by arguments.
     * @example
     * // Group by city, order by createdAt, get count
     * const result = await prisma.user.groupBy({
     *   by: ['city', 'createdAt'],
     *   orderBy: {
     *     createdAt: true
     *   },
     *   _count: {
     *     _all: true
     *   },
     * })
     * 
    **/
    groupBy<
      T extends UserClientGroupByArgs,
      HasSelectOrTake extends Or<
        Extends<'skip', Keys<T>>,
        Extends<'take', Keys<T>>
      >,
      OrderByArg extends True extends HasSelectOrTake
        ? { orderBy: UserClientGroupByArgs['orderBy'] }
        : { orderBy?: UserClientGroupByArgs['orderBy'] },
      OrderFields extends ExcludeUnderscoreKeys<Keys<MaybeTupleToUnion<T['orderBy']>>>,
      ByFields extends MaybeTupleToUnion<T['by']>,
      ByValid extends Has<ByFields, OrderFields>,
      HavingFields extends GetHavingFields<T['having']>,
      HavingValid extends Has<ByFields, HavingFields>,
      ByEmpty extends T['by'] extends never[] ? True : False,
      InputErrors extends ByEmpty extends True
      ? `Error: "by" must not be empty.`
      : HavingValid extends False
      ? {
          [P in HavingFields]: P extends ByFields
            ? never
            : P extends string
            ? `Error: Field "${P}" used in "having" needs to be provided in "by".`
            : [
                Error,
                'Field ',
                P,
                ` in "having" needs to be provided in "by"`,
              ]
        }[HavingFields]
      : 'take' extends Keys<T>
      ? 'orderBy' extends Keys<T>
        ? ByValid extends True
          ? {}
          : {
              [P in OrderFields]: P extends ByFields
                ? never
                : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
            }[OrderFields]
        : 'Error: If you provide "take", you also need to provide "orderBy"'
      : 'skip' extends Keys<T>
      ? 'orderBy' extends Keys<T>
        ? ByValid extends True
          ? {}
          : {
              [P in OrderFields]: P extends ByFields
                ? never
                : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
            }[OrderFields]
        : 'Error: If you provide "skip", you also need to provide "orderBy"'
      : ByValid extends True
      ? {}
      : {
          [P in OrderFields]: P extends ByFields
            ? never
            : `Error: Field "${P}" in "orderBy" needs to be provided in "by"`
        }[OrderFields]
    >(args: SubsetIntersection<T, UserClientGroupByArgs, OrderByArg> & InputErrors): {} extends InputErrors ? GetUserClientGroupByPayload<T> : Prisma.PrismaPromise<InputErrors>
  /**
   * Fields of the UserClient model
   */
  readonly fields: UserClientFieldRefs;
  }

  /**
   * The delegate class that acts as a "Promise-like" for UserClient.
   * Why is this prefixed with `Prisma__`?
   * Because we want to prevent naming conflicts as mentioned in
   * https://github.com/prisma/prisma-client-js/issues/707
   */
  export interface Prisma__UserClientClient<T, Null = never, ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs, GlobalOmitOptions = {}> extends Prisma.PrismaPromise<T> {
    readonly [Symbol.toStringTag]: "PrismaPromise"
    user<T extends UserDefaultArgs<ExtArgs> = {}>(args?: Subset<T, UserDefaultArgs<ExtArgs>>): Prisma__UserClient<$Result.GetResult<Prisma.$UserPayload<ExtArgs>, T, "findUniqueOrThrow", GlobalOmitOptions> | Null, Null, ExtArgs, GlobalOmitOptions>
    client<T extends ClientDefaultArgs<ExtArgs> = {}>(args?: Subset<T, ClientDefaultArgs<ExtArgs>>): Prisma__ClientClient<$Result.GetResult<Prisma.$ClientPayload<ExtArgs>, T, "findUniqueOrThrow", GlobalOmitOptions> | Null, Null, ExtArgs, GlobalOmitOptions>
    /**
     * Attaches callbacks for the resolution and/or rejection of the Promise.
     * @param onfulfilled The callback to execute when the Promise is resolved.
     * @param onrejected The callback to execute when the Promise is rejected.
     * @returns A Promise for the completion of which ever callback is executed.
     */
    then<TResult1 = T, TResult2 = never>(onfulfilled?: ((value: T) => TResult1 | PromiseLike<TResult1>) | undefined | null, onrejected?: ((reason: any) => TResult2 | PromiseLike<TResult2>) | undefined | null): $Utils.JsPromise<TResult1 | TResult2>
    /**
     * Attaches a callback for only the rejection of the Promise.
     * @param onrejected The callback to execute when the Promise is rejected.
     * @returns A Promise for the completion of the callback.
     */
    catch<TResult = never>(onrejected?: ((reason: any) => TResult | PromiseLike<TResult>) | undefined | null): $Utils.JsPromise<T | TResult>
    /**
     * Attaches a callback that is invoked when the Promise is settled (fulfilled or rejected). The
     * resolved value cannot be modified from the callback.
     * @param onfinally The callback to execute when the Promise is settled (fulfilled or rejected).
     * @returns A Promise for the completion of the callback.
     */
    finally(onfinally?: (() => void) | undefined | null): $Utils.JsPromise<T>
  }




  /**
   * Fields of the UserClient model
   */
  interface UserClientFieldRefs {
    readonly id: FieldRef<"UserClient", 'Int'>
    readonly userId: FieldRef<"UserClient", 'Int'>
    readonly clientId: FieldRef<"UserClient", 'Int'>
  }
    

  // Custom InputTypes
  /**
   * UserClient findUnique
   */
  export type UserClientFindUniqueArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the UserClient
     */
    select?: UserClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the UserClient
     */
    omit?: UserClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserClientInclude<ExtArgs> | null
    /**
     * Filter, which UserClient to fetch.
     */
    where: UserClientWhereUniqueInput
  }

  /**
   * UserClient findUniqueOrThrow
   */
  export type UserClientFindUniqueOrThrowArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the UserClient
     */
    select?: UserClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the UserClient
     */
    omit?: UserClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserClientInclude<ExtArgs> | null
    /**
     * Filter, which UserClient to fetch.
     */
    where: UserClientWhereUniqueInput
  }

  /**
   * UserClient findFirst
   */
  export type UserClientFindFirstArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the UserClient
     */
    select?: UserClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the UserClient
     */
    omit?: UserClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserClientInclude<ExtArgs> | null
    /**
     * Filter, which UserClient to fetch.
     */
    where?: UserClientWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of UserClients to fetch.
     */
    orderBy?: UserClientOrderByWithRelationInput | UserClientOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for searching for UserClients.
     */
    cursor?: UserClientWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` UserClients from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` UserClients.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/distinct Distinct Docs}
     * 
     * Filter by unique combinations of UserClients.
     */
    distinct?: UserClientScalarFieldEnum | UserClientScalarFieldEnum[]
  }

  /**
   * UserClient findFirstOrThrow
   */
  export type UserClientFindFirstOrThrowArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the UserClient
     */
    select?: UserClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the UserClient
     */
    omit?: UserClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserClientInclude<ExtArgs> | null
    /**
     * Filter, which UserClient to fetch.
     */
    where?: UserClientWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of UserClients to fetch.
     */
    orderBy?: UserClientOrderByWithRelationInput | UserClientOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for searching for UserClients.
     */
    cursor?: UserClientWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` UserClients from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` UserClients.
     */
    skip?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/distinct Distinct Docs}
     * 
     * Filter by unique combinations of UserClients.
     */
    distinct?: UserClientScalarFieldEnum | UserClientScalarFieldEnum[]
  }

  /**
   * UserClient findMany
   */
  export type UserClientFindManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the UserClient
     */
    select?: UserClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the UserClient
     */
    omit?: UserClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserClientInclude<ExtArgs> | null
    /**
     * Filter, which UserClients to fetch.
     */
    where?: UserClientWhereInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/sorting Sorting Docs}
     * 
     * Determine the order of UserClients to fetch.
     */
    orderBy?: UserClientOrderByWithRelationInput | UserClientOrderByWithRelationInput[]
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination#cursor-based-pagination Cursor Docs}
     * 
     * Sets the position for listing UserClients.
     */
    cursor?: UserClientWhereUniqueInput
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Take `±n` UserClients from the position of the cursor.
     */
    take?: number
    /**
     * {@link https://www.prisma.io/docs/concepts/components/prisma-client/pagination Pagination Docs}
     * 
     * Skip the first `n` UserClients.
     */
    skip?: number
    distinct?: UserClientScalarFieldEnum | UserClientScalarFieldEnum[]
  }

  /**
   * UserClient create
   */
  export type UserClientCreateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the UserClient
     */
    select?: UserClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the UserClient
     */
    omit?: UserClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserClientInclude<ExtArgs> | null
    /**
     * The data needed to create a UserClient.
     */
    data: XOR<UserClientCreateInput, UserClientUncheckedCreateInput>
  }

  /**
   * UserClient createMany
   */
  export type UserClientCreateManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * The data used to create many UserClients.
     */
    data: UserClientCreateManyInput | UserClientCreateManyInput[]
    skipDuplicates?: boolean
  }

  /**
   * UserClient createManyAndReturn
   */
  export type UserClientCreateManyAndReturnArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the UserClient
     */
    select?: UserClientSelectCreateManyAndReturn<ExtArgs> | null
    /**
     * Omit specific fields from the UserClient
     */
    omit?: UserClientOmit<ExtArgs> | null
    /**
     * The data used to create many UserClients.
     */
    data: UserClientCreateManyInput | UserClientCreateManyInput[]
    skipDuplicates?: boolean
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserClientIncludeCreateManyAndReturn<ExtArgs> | null
  }

  /**
   * UserClient update
   */
  export type UserClientUpdateArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the UserClient
     */
    select?: UserClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the UserClient
     */
    omit?: UserClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserClientInclude<ExtArgs> | null
    /**
     * The data needed to update a UserClient.
     */
    data: XOR<UserClientUpdateInput, UserClientUncheckedUpdateInput>
    /**
     * Choose, which UserClient to update.
     */
    where: UserClientWhereUniqueInput
  }

  /**
   * UserClient updateMany
   */
  export type UserClientUpdateManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * The data used to update UserClients.
     */
    data: XOR<UserClientUpdateManyMutationInput, UserClientUncheckedUpdateManyInput>
    /**
     * Filter which UserClients to update
     */
    where?: UserClientWhereInput
    /**
     * Limit how many UserClients to update.
     */
    limit?: number
  }

  /**
   * UserClient updateManyAndReturn
   */
  export type UserClientUpdateManyAndReturnArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the UserClient
     */
    select?: UserClientSelectUpdateManyAndReturn<ExtArgs> | null
    /**
     * Omit specific fields from the UserClient
     */
    omit?: UserClientOmit<ExtArgs> | null
    /**
     * The data used to update UserClients.
     */
    data: XOR<UserClientUpdateManyMutationInput, UserClientUncheckedUpdateManyInput>
    /**
     * Filter which UserClients to update
     */
    where?: UserClientWhereInput
    /**
     * Limit how many UserClients to update.
     */
    limit?: number
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserClientIncludeUpdateManyAndReturn<ExtArgs> | null
  }

  /**
   * UserClient upsert
   */
  export type UserClientUpsertArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the UserClient
     */
    select?: UserClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the UserClient
     */
    omit?: UserClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserClientInclude<ExtArgs> | null
    /**
     * The filter to search for the UserClient to update in case it exists.
     */
    where: UserClientWhereUniqueInput
    /**
     * In case the UserClient found by the `where` argument doesn't exist, create a new UserClient with this data.
     */
    create: XOR<UserClientCreateInput, UserClientUncheckedCreateInput>
    /**
     * In case the UserClient was found with the provided `where` argument, update it with this data.
     */
    update: XOR<UserClientUpdateInput, UserClientUncheckedUpdateInput>
  }

  /**
   * UserClient delete
   */
  export type UserClientDeleteArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the UserClient
     */
    select?: UserClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the UserClient
     */
    omit?: UserClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserClientInclude<ExtArgs> | null
    /**
     * Filter which UserClient to delete.
     */
    where: UserClientWhereUniqueInput
  }

  /**
   * UserClient deleteMany
   */
  export type UserClientDeleteManyArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Filter which UserClients to delete
     */
    where?: UserClientWhereInput
    /**
     * Limit how many UserClients to delete.
     */
    limit?: number
  }

  /**
   * UserClient without action
   */
  export type UserClientDefaultArgs<ExtArgs extends $Extensions.InternalArgs = $Extensions.DefaultArgs> = {
    /**
     * Select specific fields to fetch from the UserClient
     */
    select?: UserClientSelect<ExtArgs> | null
    /**
     * Omit specific fields from the UserClient
     */
    omit?: UserClientOmit<ExtArgs> | null
    /**
     * Choose, which related nodes to fetch as well
     */
    include?: UserClientInclude<ExtArgs> | null
  }


  /**
   * Enums
   */

  export const TransactionIsolationLevel: {
    ReadUncommitted: 'ReadUncommitted',
    ReadCommitted: 'ReadCommitted',
    RepeatableRead: 'RepeatableRead',
    Serializable: 'Serializable'
  };

  export type TransactionIsolationLevel = (typeof TransactionIsolationLevel)[keyof typeof TransactionIsolationLevel]


  export const UserScalarFieldEnum: {
    id: 'id',
    email: 'email',
    name: 'name',
    password: 'password',
    rol: 'rol'
  };

  export type UserScalarFieldEnum = (typeof UserScalarFieldEnum)[keyof typeof UserScalarFieldEnum]


  export const ClientScalarFieldEnum: {
    id: 'id',
    denumire: 'denumire',
    tip: 'tip',
    cui: 'cui',
    activa: 'activa',
    dataVerificarii: 'dataVerificarii',
    adresa: 'adresa',
    administratie: 'administratie',
    impozit: 'impozit',
    platitorTVA: 'platitorTVA',
    tvaLaIncasare: 'tvaLaIncasare',
    areCodTVAUE: 'areCodTVAUE',
    codTVAUE: 'codTVAUE',
    operatiuneUE: 'operatiuneUE',
    dividende: 'dividende',
    salariati: 'salariati',
    casaDeMarcat: 'casaDeMarcat',
    dataExpSediuSocial: 'dataExpSediuSocial',
    dataExpMandatAdmin: 'dataExpMandatAdmin',
    dataCertificatFiscal: 'dataCertificatFiscal',
    dataFisaPlatitor: 'dataFisaPlatitor',
    dataVectFiscal: 'dataVectFiscal',
    createdAt: 'createdAt',
    updatedAt: 'updatedAt'
  };

  export type ClientScalarFieldEnum = (typeof ClientScalarFieldEnum)[keyof typeof ClientScalarFieldEnum]


  export const DetaliiScalarFieldEnum: {
    id: 'id',
    registruUC: 'registruUC',
    registruEvFiscala: 'registruEvFiscala',
    ofSpalareBani: 'ofSpalareBani',
    regulamentOrdineInterioara: 'regulamentOrdineInterioara',
    manualPoliticiContabile: 'manualPoliticiContabile',
    adresaRevisal: 'adresaRevisal',
    parolaITM: 'parolaITM',
    depunereDeclaratiiOnline: 'depunereDeclaratiiOnline',
    accesDosarFiscal: 'accesDosarFiscal',
    clientId: 'clientId'
  };

  export type DetaliiScalarFieldEnum = (typeof DetaliiScalarFieldEnum)[keyof typeof DetaliiScalarFieldEnum]


  export const PunctDeLucruScalarFieldEnum: {
    id: 'id',
    denumire: 'denumire',
    deLa: 'deLa',
    panaLa: 'panaLa',
    administratie: 'administratie',
    registruUC: 'registruUC',
    salariati: 'salariati',
    cui: 'cui',
    casaDeMarcat: 'casaDeMarcat',
    clientId: 'clientId'
  };

  export type PunctDeLucruScalarFieldEnum = (typeof PunctDeLucruScalarFieldEnum)[keyof typeof PunctDeLucruScalarFieldEnum]


  export const IstoricScalarFieldEnum: {
    id: 'id',
    anul: 'anul',
    cifraAfaceri: 'cifraAfaceri',
    inventar: 'inventar',
    bilantSemIun: 'bilantSemIun',
    bilantAnual: 'bilantAnual',
    clientId: 'clientId'
  };

  export type IstoricScalarFieldEnum = (typeof IstoricScalarFieldEnum)[keyof typeof IstoricScalarFieldEnum]


  export const TaskScalarFieldEnum: {
    id: 'id',
    done: 'done',
    title: 'title',
    notes: 'notes',
    blocked: 'blocked',
    objective: 'objective',
    date: 'date',
    userId: 'userId',
    clientId: 'clientId'
  };

  export type TaskScalarFieldEnum = (typeof TaskScalarFieldEnum)[keyof typeof TaskScalarFieldEnum]


  export const RuleScalarFieldEnum: {
    id: 'id',
    name: 'name',
    description: 'description',
    frequency: 'frequency',
    taskTitle: 'taskTitle',
    taskNotes: 'taskNotes',
    active: 'active',
    createdAt: 'createdAt',
    updatedAt: 'updatedAt'
  };

  export type RuleScalarFieldEnum = (typeof RuleScalarFieldEnum)[keyof typeof RuleScalarFieldEnum]


  export const RuleConditionScalarFieldEnum: {
    id: 'id',
    field: 'field',
    operator: 'operator',
    value: 'value',
    ruleId: 'ruleId'
  };

  export type RuleConditionScalarFieldEnum = (typeof RuleConditionScalarFieldEnum)[keyof typeof RuleConditionScalarFieldEnum]


  export const UserClientScalarFieldEnum: {
    id: 'id',
    userId: 'userId',
    clientId: 'clientId'
  };

  export type UserClientScalarFieldEnum = (typeof UserClientScalarFieldEnum)[keyof typeof UserClientScalarFieldEnum]


  export const SortOrder: {
    asc: 'asc',
    desc: 'desc'
  };

  export type SortOrder = (typeof SortOrder)[keyof typeof SortOrder]


  export const QueryMode: {
    default: 'default',
    insensitive: 'insensitive'
  };

  export type QueryMode = (typeof QueryMode)[keyof typeof QueryMode]


  export const NullsOrder: {
    first: 'first',
    last: 'last'
  };

  export type NullsOrder = (typeof NullsOrder)[keyof typeof NullsOrder]


  /**
   * Field references
   */


  /**
   * Reference to a field of type 'Int'
   */
  export type IntFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'Int'>
    


  /**
   * Reference to a field of type 'Int[]'
   */
  export type ListIntFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'Int[]'>
    


  /**
   * Reference to a field of type 'String'
   */
  export type StringFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'String'>
    


  /**
   * Reference to a field of type 'String[]'
   */
  export type ListStringFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'String[]'>
    


  /**
   * Reference to a field of type 'Role'
   */
  export type EnumRoleFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'Role'>
    


  /**
   * Reference to a field of type 'Role[]'
   */
  export type ListEnumRoleFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'Role[]'>
    


  /**
   * Reference to a field of type 'Tip'
   */
  export type EnumTipFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'Tip'>
    


  /**
   * Reference to a field of type 'Tip[]'
   */
  export type ListEnumTipFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'Tip[]'>
    


  /**
   * Reference to a field of type 'Boolean'
   */
  export type BooleanFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'Boolean'>
    


  /**
   * Reference to a field of type 'DateTime'
   */
  export type DateTimeFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'DateTime'>
    


  /**
   * Reference to a field of type 'DateTime[]'
   */
  export type ListDateTimeFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'DateTime[]'>
    


  /**
   * Reference to a field of type 'Administratie'
   */
  export type EnumAdministratieFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'Administratie'>
    


  /**
   * Reference to a field of type 'Administratie[]'
   */
  export type ListEnumAdministratieFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'Administratie[]'>
    


  /**
   * Reference to a field of type 'Impozit'
   */
  export type EnumImpozitFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'Impozit'>
    


  /**
   * Reference to a field of type 'Impozit[]'
   */
  export type ListEnumImpozitFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'Impozit[]'>
    


  /**
   * Reference to a field of type 'DaLunarTrim'
   */
  export type EnumDaLunarTrimFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'DaLunarTrim'>
    


  /**
   * Reference to a field of type 'DaLunarTrim[]'
   */
  export type ListEnumDaLunarTrimFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'DaLunarTrim[]'>
    


  /**
   * Reference to a field of type 'DaNuNuECazul'
   */
  export type EnumDaNuNuECazulFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'DaNuNuECazul'>
    


  /**
   * Reference to a field of type 'DaNuNuECazul[]'
   */
  export type ListEnumDaNuNuECazulFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'DaNuNuECazul[]'>
    


  /**
   * Reference to a field of type 'Float'
   */
  export type FloatFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'Float'>
    


  /**
   * Reference to a field of type 'Float[]'
   */
  export type ListFloatFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'Float[]'>
    


  /**
   * Reference to a field of type 'Frequency'
   */
  export type EnumFrequencyFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'Frequency'>
    


  /**
   * Reference to a field of type 'Frequency[]'
   */
  export type ListEnumFrequencyFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'Frequency[]'>
    


  /**
   * Reference to a field of type 'ConditionOperator'
   */
  export type EnumConditionOperatorFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'ConditionOperator'>
    


  /**
   * Reference to a field of type 'ConditionOperator[]'
   */
  export type ListEnumConditionOperatorFieldRefInput<$PrismaModel> = FieldRefInputType<$PrismaModel, 'ConditionOperator[]'>
    
  /**
   * Deep Input Types
   */


  export type UserWhereInput = {
    AND?: UserWhereInput | UserWhereInput[]
    OR?: UserWhereInput[]
    NOT?: UserWhereInput | UserWhereInput[]
    id?: IntFilter<"User"> | number
    email?: StringFilter<"User"> | string
    name?: StringNullableFilter<"User"> | string | null
    password?: StringNullableFilter<"User"> | string | null
    rol?: EnumRoleFilter<"User"> | $Enums.Role
    tasks?: TaskListRelationFilter
    clients?: UserClientListRelationFilter
  }

  export type UserOrderByWithRelationInput = {
    id?: SortOrder
    email?: SortOrder
    name?: SortOrderInput | SortOrder
    password?: SortOrderInput | SortOrder
    rol?: SortOrder
    tasks?: TaskOrderByRelationAggregateInput
    clients?: UserClientOrderByRelationAggregateInput
  }

  export type UserWhereUniqueInput = Prisma.AtLeast<{
    id?: number
    email?: string
    AND?: UserWhereInput | UserWhereInput[]
    OR?: UserWhereInput[]
    NOT?: UserWhereInput | UserWhereInput[]
    name?: StringNullableFilter<"User"> | string | null
    password?: StringNullableFilter<"User"> | string | null
    rol?: EnumRoleFilter<"User"> | $Enums.Role
    tasks?: TaskListRelationFilter
    clients?: UserClientListRelationFilter
  }, "id" | "email">

  export type UserOrderByWithAggregationInput = {
    id?: SortOrder
    email?: SortOrder
    name?: SortOrderInput | SortOrder
    password?: SortOrderInput | SortOrder
    rol?: SortOrder
    _count?: UserCountOrderByAggregateInput
    _avg?: UserAvgOrderByAggregateInput
    _max?: UserMaxOrderByAggregateInput
    _min?: UserMinOrderByAggregateInput
    _sum?: UserSumOrderByAggregateInput
  }

  export type UserScalarWhereWithAggregatesInput = {
    AND?: UserScalarWhereWithAggregatesInput | UserScalarWhereWithAggregatesInput[]
    OR?: UserScalarWhereWithAggregatesInput[]
    NOT?: UserScalarWhereWithAggregatesInput | UserScalarWhereWithAggregatesInput[]
    id?: IntWithAggregatesFilter<"User"> | number
    email?: StringWithAggregatesFilter<"User"> | string
    name?: StringNullableWithAggregatesFilter<"User"> | string | null
    password?: StringNullableWithAggregatesFilter<"User"> | string | null
    rol?: EnumRoleWithAggregatesFilter<"User"> | $Enums.Role
  }

  export type ClientWhereInput = {
    AND?: ClientWhereInput | ClientWhereInput[]
    OR?: ClientWhereInput[]
    NOT?: ClientWhereInput | ClientWhereInput[]
    id?: IntFilter<"Client"> | number
    denumire?: StringFilter<"Client"> | string
    tip?: EnumTipFilter<"Client"> | $Enums.Tip
    cui?: StringFilter<"Client"> | string
    activa?: BoolFilter<"Client"> | boolean
    dataVerificarii?: DateTimeNullableFilter<"Client"> | Date | string | null
    adresa?: StringNullableFilter<"Client"> | string | null
    administratie?: EnumAdministratieFilter<"Client"> | $Enums.Administratie
    impozit?: EnumImpozitNullableFilter<"Client"> | $Enums.Impozit | null
    platitorTVA?: EnumDaLunarTrimFilter<"Client"> | $Enums.DaLunarTrim
    tvaLaIncasare?: BoolNullableFilter<"Client"> | boolean | null
    areCodTVAUE?: BoolNullableFilter<"Client"> | boolean | null
    codTVAUE?: StringNullableFilter<"Client"> | string | null
    operatiuneUE?: BoolNullableFilter<"Client"> | boolean | null
    dividende?: BoolNullableFilter<"Client"> | boolean | null
    salariati?: EnumDaLunarTrimNullableFilter<"Client"> | $Enums.DaLunarTrim | null
    casaDeMarcat?: BoolNullableFilter<"Client"> | boolean | null
    dataExpSediuSocial?: DateTimeNullableFilter<"Client"> | Date | string | null
    dataExpMandatAdmin?: DateTimeNullableFilter<"Client"> | Date | string | null
    dataCertificatFiscal?: DateTimeNullableFilter<"Client"> | Date | string | null
    dataFisaPlatitor?: DateTimeNullableFilter<"Client"> | Date | string | null
    dataVectFiscal?: DateTimeNullableFilter<"Client"> | Date | string | null
    createdAt?: DateTimeFilter<"Client"> | Date | string
    updatedAt?: DateTimeFilter<"Client"> | Date | string
    detalii?: XOR<DetaliiNullableScalarRelationFilter, DetaliiWhereInput> | null
    puncteDeLucru?: PunctDeLucruListRelationFilter
    istorice?: IstoricListRelationFilter
    tasks?: TaskListRelationFilter
    users?: UserClientListRelationFilter
  }

  export type ClientOrderByWithRelationInput = {
    id?: SortOrder
    denumire?: SortOrder
    tip?: SortOrder
    cui?: SortOrder
    activa?: SortOrder
    dataVerificarii?: SortOrderInput | SortOrder
    adresa?: SortOrderInput | SortOrder
    administratie?: SortOrder
    impozit?: SortOrderInput | SortOrder
    platitorTVA?: SortOrder
    tvaLaIncasare?: SortOrderInput | SortOrder
    areCodTVAUE?: SortOrderInput | SortOrder
    codTVAUE?: SortOrderInput | SortOrder
    operatiuneUE?: SortOrderInput | SortOrder
    dividende?: SortOrderInput | SortOrder
    salariati?: SortOrderInput | SortOrder
    casaDeMarcat?: SortOrderInput | SortOrder
    dataExpSediuSocial?: SortOrderInput | SortOrder
    dataExpMandatAdmin?: SortOrderInput | SortOrder
    dataCertificatFiscal?: SortOrderInput | SortOrder
    dataFisaPlatitor?: SortOrderInput | SortOrder
    dataVectFiscal?: SortOrderInput | SortOrder
    createdAt?: SortOrder
    updatedAt?: SortOrder
    detalii?: DetaliiOrderByWithRelationInput
    puncteDeLucru?: PunctDeLucruOrderByRelationAggregateInput
    istorice?: IstoricOrderByRelationAggregateInput
    tasks?: TaskOrderByRelationAggregateInput
    users?: UserClientOrderByRelationAggregateInput
  }

  export type ClientWhereUniqueInput = Prisma.AtLeast<{
    id?: number
    cui?: string
    AND?: ClientWhereInput | ClientWhereInput[]
    OR?: ClientWhereInput[]
    NOT?: ClientWhereInput | ClientWhereInput[]
    denumire?: StringFilter<"Client"> | string
    tip?: EnumTipFilter<"Client"> | $Enums.Tip
    activa?: BoolFilter<"Client"> | boolean
    dataVerificarii?: DateTimeNullableFilter<"Client"> | Date | string | null
    adresa?: StringNullableFilter<"Client"> | string | null
    administratie?: EnumAdministratieFilter<"Client"> | $Enums.Administratie
    impozit?: EnumImpozitNullableFilter<"Client"> | $Enums.Impozit | null
    platitorTVA?: EnumDaLunarTrimFilter<"Client"> | $Enums.DaLunarTrim
    tvaLaIncasare?: BoolNullableFilter<"Client"> | boolean | null
    areCodTVAUE?: BoolNullableFilter<"Client"> | boolean | null
    codTVAUE?: StringNullableFilter<"Client"> | string | null
    operatiuneUE?: BoolNullableFilter<"Client"> | boolean | null
    dividende?: BoolNullableFilter<"Client"> | boolean | null
    salariati?: EnumDaLunarTrimNullableFilter<"Client"> | $Enums.DaLunarTrim | null
    casaDeMarcat?: BoolNullableFilter<"Client"> | boolean | null
    dataExpSediuSocial?: DateTimeNullableFilter<"Client"> | Date | string | null
    dataExpMandatAdmin?: DateTimeNullableFilter<"Client"> | Date | string | null
    dataCertificatFiscal?: DateTimeNullableFilter<"Client"> | Date | string | null
    dataFisaPlatitor?: DateTimeNullableFilter<"Client"> | Date | string | null
    dataVectFiscal?: DateTimeNullableFilter<"Client"> | Date | string | null
    createdAt?: DateTimeFilter<"Client"> | Date | string
    updatedAt?: DateTimeFilter<"Client"> | Date | string
    detalii?: XOR<DetaliiNullableScalarRelationFilter, DetaliiWhereInput> | null
    puncteDeLucru?: PunctDeLucruListRelationFilter
    istorice?: IstoricListRelationFilter
    tasks?: TaskListRelationFilter
    users?: UserClientListRelationFilter
  }, "id" | "cui">

  export type ClientOrderByWithAggregationInput = {
    id?: SortOrder
    denumire?: SortOrder
    tip?: SortOrder
    cui?: SortOrder
    activa?: SortOrder
    dataVerificarii?: SortOrderInput | SortOrder
    adresa?: SortOrderInput | SortOrder
    administratie?: SortOrder
    impozit?: SortOrderInput | SortOrder
    platitorTVA?: SortOrder
    tvaLaIncasare?: SortOrderInput | SortOrder
    areCodTVAUE?: SortOrderInput | SortOrder
    codTVAUE?: SortOrderInput | SortOrder
    operatiuneUE?: SortOrderInput | SortOrder
    dividende?: SortOrderInput | SortOrder
    salariati?: SortOrderInput | SortOrder
    casaDeMarcat?: SortOrderInput | SortOrder
    dataExpSediuSocial?: SortOrderInput | SortOrder
    dataExpMandatAdmin?: SortOrderInput | SortOrder
    dataCertificatFiscal?: SortOrderInput | SortOrder
    dataFisaPlatitor?: SortOrderInput | SortOrder
    dataVectFiscal?: SortOrderInput | SortOrder
    createdAt?: SortOrder
    updatedAt?: SortOrder
    _count?: ClientCountOrderByAggregateInput
    _avg?: ClientAvgOrderByAggregateInput
    _max?: ClientMaxOrderByAggregateInput
    _min?: ClientMinOrderByAggregateInput
    _sum?: ClientSumOrderByAggregateInput
  }

  export type ClientScalarWhereWithAggregatesInput = {
    AND?: ClientScalarWhereWithAggregatesInput | ClientScalarWhereWithAggregatesInput[]
    OR?: ClientScalarWhereWithAggregatesInput[]
    NOT?: ClientScalarWhereWithAggregatesInput | ClientScalarWhereWithAggregatesInput[]
    id?: IntWithAggregatesFilter<"Client"> | number
    denumire?: StringWithAggregatesFilter<"Client"> | string
    tip?: EnumTipWithAggregatesFilter<"Client"> | $Enums.Tip
    cui?: StringWithAggregatesFilter<"Client"> | string
    activa?: BoolWithAggregatesFilter<"Client"> | boolean
    dataVerificarii?: DateTimeNullableWithAggregatesFilter<"Client"> | Date | string | null
    adresa?: StringNullableWithAggregatesFilter<"Client"> | string | null
    administratie?: EnumAdministratieWithAggregatesFilter<"Client"> | $Enums.Administratie
    impozit?: EnumImpozitNullableWithAggregatesFilter<"Client"> | $Enums.Impozit | null
    platitorTVA?: EnumDaLunarTrimWithAggregatesFilter<"Client"> | $Enums.DaLunarTrim
    tvaLaIncasare?: BoolNullableWithAggregatesFilter<"Client"> | boolean | null
    areCodTVAUE?: BoolNullableWithAggregatesFilter<"Client"> | boolean | null
    codTVAUE?: StringNullableWithAggregatesFilter<"Client"> | string | null
    operatiuneUE?: BoolNullableWithAggregatesFilter<"Client"> | boolean | null
    dividende?: BoolNullableWithAggregatesFilter<"Client"> | boolean | null
    salariati?: EnumDaLunarTrimNullableWithAggregatesFilter<"Client"> | $Enums.DaLunarTrim | null
    casaDeMarcat?: BoolNullableWithAggregatesFilter<"Client"> | boolean | null
    dataExpSediuSocial?: DateTimeNullableWithAggregatesFilter<"Client"> | Date | string | null
    dataExpMandatAdmin?: DateTimeNullableWithAggregatesFilter<"Client"> | Date | string | null
    dataCertificatFiscal?: DateTimeNullableWithAggregatesFilter<"Client"> | Date | string | null
    dataFisaPlatitor?: DateTimeNullableWithAggregatesFilter<"Client"> | Date | string | null
    dataVectFiscal?: DateTimeNullableWithAggregatesFilter<"Client"> | Date | string | null
    createdAt?: DateTimeWithAggregatesFilter<"Client"> | Date | string
    updatedAt?: DateTimeWithAggregatesFilter<"Client"> | Date | string
  }

  export type DetaliiWhereInput = {
    AND?: DetaliiWhereInput | DetaliiWhereInput[]
    OR?: DetaliiWhereInput[]
    NOT?: DetaliiWhereInput | DetaliiWhereInput[]
    id?: IntFilter<"Detalii"> | number
    registruUC?: BoolFilter<"Detalii"> | boolean
    registruEvFiscala?: EnumDaNuNuECazulFilter<"Detalii"> | $Enums.DaNuNuECazul
    ofSpalareBani?: BoolFilter<"Detalii"> | boolean
    regulamentOrdineInterioara?: BoolFilter<"Detalii"> | boolean
    manualPoliticiContabile?: BoolFilter<"Detalii"> | boolean
    adresaRevisal?: BoolFilter<"Detalii"> | boolean
    parolaITM?: StringNullableFilter<"Detalii"> | string | null
    depunereDeclaratiiOnline?: BoolFilter<"Detalii"> | boolean
    accesDosarFiscal?: EnumDaNuNuECazulFilter<"Detalii"> | $Enums.DaNuNuECazul
    clientId?: IntFilter<"Detalii"> | number
    client?: XOR<ClientScalarRelationFilter, ClientWhereInput>
  }

  export type DetaliiOrderByWithRelationInput = {
    id?: SortOrder
    registruUC?: SortOrder
    registruEvFiscala?: SortOrder
    ofSpalareBani?: SortOrder
    regulamentOrdineInterioara?: SortOrder
    manualPoliticiContabile?: SortOrder
    adresaRevisal?: SortOrder
    parolaITM?: SortOrderInput | SortOrder
    depunereDeclaratiiOnline?: SortOrder
    accesDosarFiscal?: SortOrder
    clientId?: SortOrder
    client?: ClientOrderByWithRelationInput
  }

  export type DetaliiWhereUniqueInput = Prisma.AtLeast<{
    id?: number
    clientId?: number
    AND?: DetaliiWhereInput | DetaliiWhereInput[]
    OR?: DetaliiWhereInput[]
    NOT?: DetaliiWhereInput | DetaliiWhereInput[]
    registruUC?: BoolFilter<"Detalii"> | boolean
    registruEvFiscala?: EnumDaNuNuECazulFilter<"Detalii"> | $Enums.DaNuNuECazul
    ofSpalareBani?: BoolFilter<"Detalii"> | boolean
    regulamentOrdineInterioara?: BoolFilter<"Detalii"> | boolean
    manualPoliticiContabile?: BoolFilter<"Detalii"> | boolean
    adresaRevisal?: BoolFilter<"Detalii"> | boolean
    parolaITM?: StringNullableFilter<"Detalii"> | string | null
    depunereDeclaratiiOnline?: BoolFilter<"Detalii"> | boolean
    accesDosarFiscal?: EnumDaNuNuECazulFilter<"Detalii"> | $Enums.DaNuNuECazul
    client?: XOR<ClientScalarRelationFilter, ClientWhereInput>
  }, "id" | "clientId">

  export type DetaliiOrderByWithAggregationInput = {
    id?: SortOrder
    registruUC?: SortOrder
    registruEvFiscala?: SortOrder
    ofSpalareBani?: SortOrder
    regulamentOrdineInterioara?: SortOrder
    manualPoliticiContabile?: SortOrder
    adresaRevisal?: SortOrder
    parolaITM?: SortOrderInput | SortOrder
    depunereDeclaratiiOnline?: SortOrder
    accesDosarFiscal?: SortOrder
    clientId?: SortOrder
    _count?: DetaliiCountOrderByAggregateInput
    _avg?: DetaliiAvgOrderByAggregateInput
    _max?: DetaliiMaxOrderByAggregateInput
    _min?: DetaliiMinOrderByAggregateInput
    _sum?: DetaliiSumOrderByAggregateInput
  }

  export type DetaliiScalarWhereWithAggregatesInput = {
    AND?: DetaliiScalarWhereWithAggregatesInput | DetaliiScalarWhereWithAggregatesInput[]
    OR?: DetaliiScalarWhereWithAggregatesInput[]
    NOT?: DetaliiScalarWhereWithAggregatesInput | DetaliiScalarWhereWithAggregatesInput[]
    id?: IntWithAggregatesFilter<"Detalii"> | number
    registruUC?: BoolWithAggregatesFilter<"Detalii"> | boolean
    registruEvFiscala?: EnumDaNuNuECazulWithAggregatesFilter<"Detalii"> | $Enums.DaNuNuECazul
    ofSpalareBani?: BoolWithAggregatesFilter<"Detalii"> | boolean
    regulamentOrdineInterioara?: BoolWithAggregatesFilter<"Detalii"> | boolean
    manualPoliticiContabile?: BoolWithAggregatesFilter<"Detalii"> | boolean
    adresaRevisal?: BoolWithAggregatesFilter<"Detalii"> | boolean
    parolaITM?: StringNullableWithAggregatesFilter<"Detalii"> | string | null
    depunereDeclaratiiOnline?: BoolWithAggregatesFilter<"Detalii"> | boolean
    accesDosarFiscal?: EnumDaNuNuECazulWithAggregatesFilter<"Detalii"> | $Enums.DaNuNuECazul
    clientId?: IntWithAggregatesFilter<"Detalii"> | number
  }

  export type PunctDeLucruWhereInput = {
    AND?: PunctDeLucruWhereInput | PunctDeLucruWhereInput[]
    OR?: PunctDeLucruWhereInput[]
    NOT?: PunctDeLucruWhereInput | PunctDeLucruWhereInput[]
    id?: IntFilter<"PunctDeLucru"> | number
    denumire?: StringFilter<"PunctDeLucru"> | string
    deLa?: DateTimeFilter<"PunctDeLucru"> | Date | string
    panaLa?: DateTimeNullableFilter<"PunctDeLucru"> | Date | string | null
    administratie?: EnumAdministratieFilter<"PunctDeLucru"> | $Enums.Administratie
    registruUC?: BoolFilter<"PunctDeLucru"> | boolean
    salariati?: IntFilter<"PunctDeLucru"> | number
    cui?: StringNullableFilter<"PunctDeLucru"> | string | null
    casaDeMarcat?: BoolFilter<"PunctDeLucru"> | boolean
    clientId?: IntFilter<"PunctDeLucru"> | number
    client?: XOR<ClientScalarRelationFilter, ClientWhereInput>
  }

  export type PunctDeLucruOrderByWithRelationInput = {
    id?: SortOrder
    denumire?: SortOrder
    deLa?: SortOrder
    panaLa?: SortOrderInput | SortOrder
    administratie?: SortOrder
    registruUC?: SortOrder
    salariati?: SortOrder
    cui?: SortOrderInput | SortOrder
    casaDeMarcat?: SortOrder
    clientId?: SortOrder
    client?: ClientOrderByWithRelationInput
  }

  export type PunctDeLucruWhereUniqueInput = Prisma.AtLeast<{
    id?: number
    AND?: PunctDeLucruWhereInput | PunctDeLucruWhereInput[]
    OR?: PunctDeLucruWhereInput[]
    NOT?: PunctDeLucruWhereInput | PunctDeLucruWhereInput[]
    denumire?: StringFilter<"PunctDeLucru"> | string
    deLa?: DateTimeFilter<"PunctDeLucru"> | Date | string
    panaLa?: DateTimeNullableFilter<"PunctDeLucru"> | Date | string | null
    administratie?: EnumAdministratieFilter<"PunctDeLucru"> | $Enums.Administratie
    registruUC?: BoolFilter<"PunctDeLucru"> | boolean
    salariati?: IntFilter<"PunctDeLucru"> | number
    cui?: StringNullableFilter<"PunctDeLucru"> | string | null
    casaDeMarcat?: BoolFilter<"PunctDeLucru"> | boolean
    clientId?: IntFilter<"PunctDeLucru"> | number
    client?: XOR<ClientScalarRelationFilter, ClientWhereInput>
  }, "id">

  export type PunctDeLucruOrderByWithAggregationInput = {
    id?: SortOrder
    denumire?: SortOrder
    deLa?: SortOrder
    panaLa?: SortOrderInput | SortOrder
    administratie?: SortOrder
    registruUC?: SortOrder
    salariati?: SortOrder
    cui?: SortOrderInput | SortOrder
    casaDeMarcat?: SortOrder
    clientId?: SortOrder
    _count?: PunctDeLucruCountOrderByAggregateInput
    _avg?: PunctDeLucruAvgOrderByAggregateInput
    _max?: PunctDeLucruMaxOrderByAggregateInput
    _min?: PunctDeLucruMinOrderByAggregateInput
    _sum?: PunctDeLucruSumOrderByAggregateInput
  }

  export type PunctDeLucruScalarWhereWithAggregatesInput = {
    AND?: PunctDeLucruScalarWhereWithAggregatesInput | PunctDeLucruScalarWhereWithAggregatesInput[]
    OR?: PunctDeLucruScalarWhereWithAggregatesInput[]
    NOT?: PunctDeLucruScalarWhereWithAggregatesInput | PunctDeLucruScalarWhereWithAggregatesInput[]
    id?: IntWithAggregatesFilter<"PunctDeLucru"> | number
    denumire?: StringWithAggregatesFilter<"PunctDeLucru"> | string
    deLa?: DateTimeWithAggregatesFilter<"PunctDeLucru"> | Date | string
    panaLa?: DateTimeNullableWithAggregatesFilter<"PunctDeLucru"> | Date | string | null
    administratie?: EnumAdministratieWithAggregatesFilter<"PunctDeLucru"> | $Enums.Administratie
    registruUC?: BoolWithAggregatesFilter<"PunctDeLucru"> | boolean
    salariati?: IntWithAggregatesFilter<"PunctDeLucru"> | number
    cui?: StringNullableWithAggregatesFilter<"PunctDeLucru"> | string | null
    casaDeMarcat?: BoolWithAggregatesFilter<"PunctDeLucru"> | boolean
    clientId?: IntWithAggregatesFilter<"PunctDeLucru"> | number
  }

  export type IstoricWhereInput = {
    AND?: IstoricWhereInput | IstoricWhereInput[]
    OR?: IstoricWhereInput[]
    NOT?: IstoricWhereInput | IstoricWhereInput[]
    id?: IntFilter<"Istoric"> | number
    anul?: IntFilter<"Istoric"> | number
    cifraAfaceri?: FloatFilter<"Istoric"> | number
    inventar?: BoolFilter<"Istoric"> | boolean
    bilantSemIun?: EnumDaNuNuECazulFilter<"Istoric"> | $Enums.DaNuNuECazul
    bilantAnual?: EnumDaNuNuECazulFilter<"Istoric"> | $Enums.DaNuNuECazul
    clientId?: IntFilter<"Istoric"> | number
    client?: XOR<ClientScalarRelationFilter, ClientWhereInput>
  }

  export type IstoricOrderByWithRelationInput = {
    id?: SortOrder
    anul?: SortOrder
    cifraAfaceri?: SortOrder
    inventar?: SortOrder
    bilantSemIun?: SortOrder
    bilantAnual?: SortOrder
    clientId?: SortOrder
    client?: ClientOrderByWithRelationInput
  }

  export type IstoricWhereUniqueInput = Prisma.AtLeast<{
    id?: number
    clientId_anul?: IstoricClientIdAnulCompoundUniqueInput
    AND?: IstoricWhereInput | IstoricWhereInput[]
    OR?: IstoricWhereInput[]
    NOT?: IstoricWhereInput | IstoricWhereInput[]
    anul?: IntFilter<"Istoric"> | number
    cifraAfaceri?: FloatFilter<"Istoric"> | number
    inventar?: BoolFilter<"Istoric"> | boolean
    bilantSemIun?: EnumDaNuNuECazulFilter<"Istoric"> | $Enums.DaNuNuECazul
    bilantAnual?: EnumDaNuNuECazulFilter<"Istoric"> | $Enums.DaNuNuECazul
    clientId?: IntFilter<"Istoric"> | number
    client?: XOR<ClientScalarRelationFilter, ClientWhereInput>
  }, "id" | "clientId_anul">

  export type IstoricOrderByWithAggregationInput = {
    id?: SortOrder
    anul?: SortOrder
    cifraAfaceri?: SortOrder
    inventar?: SortOrder
    bilantSemIun?: SortOrder
    bilantAnual?: SortOrder
    clientId?: SortOrder
    _count?: IstoricCountOrderByAggregateInput
    _avg?: IstoricAvgOrderByAggregateInput
    _max?: IstoricMaxOrderByAggregateInput
    _min?: IstoricMinOrderByAggregateInput
    _sum?: IstoricSumOrderByAggregateInput
  }

  export type IstoricScalarWhereWithAggregatesInput = {
    AND?: IstoricScalarWhereWithAggregatesInput | IstoricScalarWhereWithAggregatesInput[]
    OR?: IstoricScalarWhereWithAggregatesInput[]
    NOT?: IstoricScalarWhereWithAggregatesInput | IstoricScalarWhereWithAggregatesInput[]
    id?: IntWithAggregatesFilter<"Istoric"> | number
    anul?: IntWithAggregatesFilter<"Istoric"> | number
    cifraAfaceri?: FloatWithAggregatesFilter<"Istoric"> | number
    inventar?: BoolWithAggregatesFilter<"Istoric"> | boolean
    bilantSemIun?: EnumDaNuNuECazulWithAggregatesFilter<"Istoric"> | $Enums.DaNuNuECazul
    bilantAnual?: EnumDaNuNuECazulWithAggregatesFilter<"Istoric"> | $Enums.DaNuNuECazul
    clientId?: IntWithAggregatesFilter<"Istoric"> | number
  }

  export type TaskWhereInput = {
    AND?: TaskWhereInput | TaskWhereInput[]
    OR?: TaskWhereInput[]
    NOT?: TaskWhereInput | TaskWhereInput[]
    id?: IntFilter<"Task"> | number
    done?: BoolFilter<"Task"> | boolean
    title?: StringFilter<"Task"> | string
    notes?: StringNullableFilter<"Task"> | string | null
    blocked?: StringNullableFilter<"Task"> | string | null
    objective?: StringNullableFilter<"Task"> | string | null
    date?: DateTimeFilter<"Task"> | Date | string
    userId?: IntFilter<"Task"> | number
    clientId?: IntFilter<"Task"> | number
    user?: XOR<UserScalarRelationFilter, UserWhereInput>
    client?: XOR<ClientScalarRelationFilter, ClientWhereInput>
  }

  export type TaskOrderByWithRelationInput = {
    id?: SortOrder
    done?: SortOrder
    title?: SortOrder
    notes?: SortOrderInput | SortOrder
    blocked?: SortOrderInput | SortOrder
    objective?: SortOrderInput | SortOrder
    date?: SortOrder
    userId?: SortOrder
    clientId?: SortOrder
    user?: UserOrderByWithRelationInput
    client?: ClientOrderByWithRelationInput
  }

  export type TaskWhereUniqueInput = Prisma.AtLeast<{
    id?: number
    AND?: TaskWhereInput | TaskWhereInput[]
    OR?: TaskWhereInput[]
    NOT?: TaskWhereInput | TaskWhereInput[]
    done?: BoolFilter<"Task"> | boolean
    title?: StringFilter<"Task"> | string
    notes?: StringNullableFilter<"Task"> | string | null
    blocked?: StringNullableFilter<"Task"> | string | null
    objective?: StringNullableFilter<"Task"> | string | null
    date?: DateTimeFilter<"Task"> | Date | string
    userId?: IntFilter<"Task"> | number
    clientId?: IntFilter<"Task"> | number
    user?: XOR<UserScalarRelationFilter, UserWhereInput>
    client?: XOR<ClientScalarRelationFilter, ClientWhereInput>
  }, "id">

  export type TaskOrderByWithAggregationInput = {
    id?: SortOrder
    done?: SortOrder
    title?: SortOrder
    notes?: SortOrderInput | SortOrder
    blocked?: SortOrderInput | SortOrder
    objective?: SortOrderInput | SortOrder
    date?: SortOrder
    userId?: SortOrder
    clientId?: SortOrder
    _count?: TaskCountOrderByAggregateInput
    _avg?: TaskAvgOrderByAggregateInput
    _max?: TaskMaxOrderByAggregateInput
    _min?: TaskMinOrderByAggregateInput
    _sum?: TaskSumOrderByAggregateInput
  }

  export type TaskScalarWhereWithAggregatesInput = {
    AND?: TaskScalarWhereWithAggregatesInput | TaskScalarWhereWithAggregatesInput[]
    OR?: TaskScalarWhereWithAggregatesInput[]
    NOT?: TaskScalarWhereWithAggregatesInput | TaskScalarWhereWithAggregatesInput[]
    id?: IntWithAggregatesFilter<"Task"> | number
    done?: BoolWithAggregatesFilter<"Task"> | boolean
    title?: StringWithAggregatesFilter<"Task"> | string
    notes?: StringNullableWithAggregatesFilter<"Task"> | string | null
    blocked?: StringNullableWithAggregatesFilter<"Task"> | string | null
    objective?: StringNullableWithAggregatesFilter<"Task"> | string | null
    date?: DateTimeWithAggregatesFilter<"Task"> | Date | string
    userId?: IntWithAggregatesFilter<"Task"> | number
    clientId?: IntWithAggregatesFilter<"Task"> | number
  }

  export type RuleWhereInput = {
    AND?: RuleWhereInput | RuleWhereInput[]
    OR?: RuleWhereInput[]
    NOT?: RuleWhereInput | RuleWhereInput[]
    id?: IntFilter<"Rule"> | number
    name?: StringFilter<"Rule"> | string
    description?: StringNullableFilter<"Rule"> | string | null
    frequency?: EnumFrequencyFilter<"Rule"> | $Enums.Frequency
    taskTitle?: StringFilter<"Rule"> | string
    taskNotes?: StringNullableFilter<"Rule"> | string | null
    active?: BoolFilter<"Rule"> | boolean
    createdAt?: DateTimeFilter<"Rule"> | Date | string
    updatedAt?: DateTimeFilter<"Rule"> | Date | string
    conditions?: RuleConditionListRelationFilter
  }

  export type RuleOrderByWithRelationInput = {
    id?: SortOrder
    name?: SortOrder
    description?: SortOrderInput | SortOrder
    frequency?: SortOrder
    taskTitle?: SortOrder
    taskNotes?: SortOrderInput | SortOrder
    active?: SortOrder
    createdAt?: SortOrder
    updatedAt?: SortOrder
    conditions?: RuleConditionOrderByRelationAggregateInput
  }

  export type RuleWhereUniqueInput = Prisma.AtLeast<{
    id?: number
    name?: string
    AND?: RuleWhereInput | RuleWhereInput[]
    OR?: RuleWhereInput[]
    NOT?: RuleWhereInput | RuleWhereInput[]
    description?: StringNullableFilter<"Rule"> | string | null
    frequency?: EnumFrequencyFilter<"Rule"> | $Enums.Frequency
    taskTitle?: StringFilter<"Rule"> | string
    taskNotes?: StringNullableFilter<"Rule"> | string | null
    active?: BoolFilter<"Rule"> | boolean
    createdAt?: DateTimeFilter<"Rule"> | Date | string
    updatedAt?: DateTimeFilter<"Rule"> | Date | string
    conditions?: RuleConditionListRelationFilter
  }, "id" | "name">

  export type RuleOrderByWithAggregationInput = {
    id?: SortOrder
    name?: SortOrder
    description?: SortOrderInput | SortOrder
    frequency?: SortOrder
    taskTitle?: SortOrder
    taskNotes?: SortOrderInput | SortOrder
    active?: SortOrder
    createdAt?: SortOrder
    updatedAt?: SortOrder
    _count?: RuleCountOrderByAggregateInput
    _avg?: RuleAvgOrderByAggregateInput
    _max?: RuleMaxOrderByAggregateInput
    _min?: RuleMinOrderByAggregateInput
    _sum?: RuleSumOrderByAggregateInput
  }

  export type RuleScalarWhereWithAggregatesInput = {
    AND?: RuleScalarWhereWithAggregatesInput | RuleScalarWhereWithAggregatesInput[]
    OR?: RuleScalarWhereWithAggregatesInput[]
    NOT?: RuleScalarWhereWithAggregatesInput | RuleScalarWhereWithAggregatesInput[]
    id?: IntWithAggregatesFilter<"Rule"> | number
    name?: StringWithAggregatesFilter<"Rule"> | string
    description?: StringNullableWithAggregatesFilter<"Rule"> | string | null
    frequency?: EnumFrequencyWithAggregatesFilter<"Rule"> | $Enums.Frequency
    taskTitle?: StringWithAggregatesFilter<"Rule"> | string
    taskNotes?: StringNullableWithAggregatesFilter<"Rule"> | string | null
    active?: BoolWithAggregatesFilter<"Rule"> | boolean
    createdAt?: DateTimeWithAggregatesFilter<"Rule"> | Date | string
    updatedAt?: DateTimeWithAggregatesFilter<"Rule"> | Date | string
  }

  export type RuleConditionWhereInput = {
    AND?: RuleConditionWhereInput | RuleConditionWhereInput[]
    OR?: RuleConditionWhereInput[]
    NOT?: RuleConditionWhereInput | RuleConditionWhereInput[]
    id?: IntFilter<"RuleCondition"> | number
    field?: StringFilter<"RuleCondition"> | string
    operator?: EnumConditionOperatorFilter<"RuleCondition"> | $Enums.ConditionOperator
    value?: StringFilter<"RuleCondition"> | string
    ruleId?: IntFilter<"RuleCondition"> | number
    rule?: XOR<RuleScalarRelationFilter, RuleWhereInput>
  }

  export type RuleConditionOrderByWithRelationInput = {
    id?: SortOrder
    field?: SortOrder
    operator?: SortOrder
    value?: SortOrder
    ruleId?: SortOrder
    rule?: RuleOrderByWithRelationInput
  }

  export type RuleConditionWhereUniqueInput = Prisma.AtLeast<{
    id?: number
    AND?: RuleConditionWhereInput | RuleConditionWhereInput[]
    OR?: RuleConditionWhereInput[]
    NOT?: RuleConditionWhereInput | RuleConditionWhereInput[]
    field?: StringFilter<"RuleCondition"> | string
    operator?: EnumConditionOperatorFilter<"RuleCondition"> | $Enums.ConditionOperator
    value?: StringFilter<"RuleCondition"> | string
    ruleId?: IntFilter<"RuleCondition"> | number
    rule?: XOR<RuleScalarRelationFilter, RuleWhereInput>
  }, "id">

  export type RuleConditionOrderByWithAggregationInput = {
    id?: SortOrder
    field?: SortOrder
    operator?: SortOrder
    value?: SortOrder
    ruleId?: SortOrder
    _count?: RuleConditionCountOrderByAggregateInput
    _avg?: RuleConditionAvgOrderByAggregateInput
    _max?: RuleConditionMaxOrderByAggregateInput
    _min?: RuleConditionMinOrderByAggregateInput
    _sum?: RuleConditionSumOrderByAggregateInput
  }

  export type RuleConditionScalarWhereWithAggregatesInput = {
    AND?: RuleConditionScalarWhereWithAggregatesInput | RuleConditionScalarWhereWithAggregatesInput[]
    OR?: RuleConditionScalarWhereWithAggregatesInput[]
    NOT?: RuleConditionScalarWhereWithAggregatesInput | RuleConditionScalarWhereWithAggregatesInput[]
    id?: IntWithAggregatesFilter<"RuleCondition"> | number
    field?: StringWithAggregatesFilter<"RuleCondition"> | string
    operator?: EnumConditionOperatorWithAggregatesFilter<"RuleCondition"> | $Enums.ConditionOperator
    value?: StringWithAggregatesFilter<"RuleCondition"> | string
    ruleId?: IntWithAggregatesFilter<"RuleCondition"> | number
  }

  export type UserClientWhereInput = {
    AND?: UserClientWhereInput | UserClientWhereInput[]
    OR?: UserClientWhereInput[]
    NOT?: UserClientWhereInput | UserClientWhereInput[]
    id?: IntFilter<"UserClient"> | number
    userId?: IntFilter<"UserClient"> | number
    clientId?: IntFilter<"UserClient"> | number
    user?: XOR<UserScalarRelationFilter, UserWhereInput>
    client?: XOR<ClientScalarRelationFilter, ClientWhereInput>
  }

  export type UserClientOrderByWithRelationInput = {
    id?: SortOrder
    userId?: SortOrder
    clientId?: SortOrder
    user?: UserOrderByWithRelationInput
    client?: ClientOrderByWithRelationInput
  }

  export type UserClientWhereUniqueInput = Prisma.AtLeast<{
    id?: number
    userId_clientId?: UserClientUserIdClientIdCompoundUniqueInput
    AND?: UserClientWhereInput | UserClientWhereInput[]
    OR?: UserClientWhereInput[]
    NOT?: UserClientWhereInput | UserClientWhereInput[]
    userId?: IntFilter<"UserClient"> | number
    clientId?: IntFilter<"UserClient"> | number
    user?: XOR<UserScalarRelationFilter, UserWhereInput>
    client?: XOR<ClientScalarRelationFilter, ClientWhereInput>
  }, "id" | "userId_clientId">

  export type UserClientOrderByWithAggregationInput = {
    id?: SortOrder
    userId?: SortOrder
    clientId?: SortOrder
    _count?: UserClientCountOrderByAggregateInput
    _avg?: UserClientAvgOrderByAggregateInput
    _max?: UserClientMaxOrderByAggregateInput
    _min?: UserClientMinOrderByAggregateInput
    _sum?: UserClientSumOrderByAggregateInput
  }

  export type UserClientScalarWhereWithAggregatesInput = {
    AND?: UserClientScalarWhereWithAggregatesInput | UserClientScalarWhereWithAggregatesInput[]
    OR?: UserClientScalarWhereWithAggregatesInput[]
    NOT?: UserClientScalarWhereWithAggregatesInput | UserClientScalarWhereWithAggregatesInput[]
    id?: IntWithAggregatesFilter<"UserClient"> | number
    userId?: IntWithAggregatesFilter<"UserClient"> | number
    clientId?: IntWithAggregatesFilter<"UserClient"> | number
  }

  export type UserCreateInput = {
    email: string
    name?: string | null
    password?: string | null
    rol?: $Enums.Role
    tasks?: TaskCreateNestedManyWithoutUserInput
    clients?: UserClientCreateNestedManyWithoutUserInput
  }

  export type UserUncheckedCreateInput = {
    id?: number
    email: string
    name?: string | null
    password?: string | null
    rol?: $Enums.Role
    tasks?: TaskUncheckedCreateNestedManyWithoutUserInput
    clients?: UserClientUncheckedCreateNestedManyWithoutUserInput
  }

  export type UserUpdateInput = {
    email?: StringFieldUpdateOperationsInput | string
    name?: NullableStringFieldUpdateOperationsInput | string | null
    password?: NullableStringFieldUpdateOperationsInput | string | null
    rol?: EnumRoleFieldUpdateOperationsInput | $Enums.Role
    tasks?: TaskUpdateManyWithoutUserNestedInput
    clients?: UserClientUpdateManyWithoutUserNestedInput
  }

  export type UserUncheckedUpdateInput = {
    id?: IntFieldUpdateOperationsInput | number
    email?: StringFieldUpdateOperationsInput | string
    name?: NullableStringFieldUpdateOperationsInput | string | null
    password?: NullableStringFieldUpdateOperationsInput | string | null
    rol?: EnumRoleFieldUpdateOperationsInput | $Enums.Role
    tasks?: TaskUncheckedUpdateManyWithoutUserNestedInput
    clients?: UserClientUncheckedUpdateManyWithoutUserNestedInput
  }

  export type UserCreateManyInput = {
    id?: number
    email: string
    name?: string | null
    password?: string | null
    rol?: $Enums.Role
  }

  export type UserUpdateManyMutationInput = {
    email?: StringFieldUpdateOperationsInput | string
    name?: NullableStringFieldUpdateOperationsInput | string | null
    password?: NullableStringFieldUpdateOperationsInput | string | null
    rol?: EnumRoleFieldUpdateOperationsInput | $Enums.Role
  }

  export type UserUncheckedUpdateManyInput = {
    id?: IntFieldUpdateOperationsInput | number
    email?: StringFieldUpdateOperationsInput | string
    name?: NullableStringFieldUpdateOperationsInput | string | null
    password?: NullableStringFieldUpdateOperationsInput | string | null
    rol?: EnumRoleFieldUpdateOperationsInput | $Enums.Role
  }

  export type ClientCreateInput = {
    denumire: string
    tip: $Enums.Tip
    cui: string
    activa: boolean
    dataVerificarii?: Date | string | null
    adresa?: string | null
    administratie: $Enums.Administratie
    impozit?: $Enums.Impozit | null
    platitorTVA: $Enums.DaLunarTrim
    tvaLaIncasare?: boolean | null
    areCodTVAUE?: boolean | null
    codTVAUE?: string | null
    operatiuneUE?: boolean | null
    dividende?: boolean | null
    salariati?: $Enums.DaLunarTrim | null
    casaDeMarcat?: boolean | null
    dataExpSediuSocial?: Date | string | null
    dataExpMandatAdmin?: Date | string | null
    dataCertificatFiscal?: Date | string | null
    dataFisaPlatitor?: Date | string | null
    dataVectFiscal?: Date | string | null
    createdAt?: Date | string
    updatedAt?: Date | string
    detalii?: DetaliiCreateNestedOneWithoutClientInput
    puncteDeLucru?: PunctDeLucruCreateNestedManyWithoutClientInput
    istorice?: IstoricCreateNestedManyWithoutClientInput
    tasks?: TaskCreateNestedManyWithoutClientInput
    users?: UserClientCreateNestedManyWithoutClientInput
  }

  export type ClientUncheckedCreateInput = {
    id?: number
    denumire: string
    tip: $Enums.Tip
    cui: string
    activa: boolean
    dataVerificarii?: Date | string | null
    adresa?: string | null
    administratie: $Enums.Administratie
    impozit?: $Enums.Impozit | null
    platitorTVA: $Enums.DaLunarTrim
    tvaLaIncasare?: boolean | null
    areCodTVAUE?: boolean | null
    codTVAUE?: string | null
    operatiuneUE?: boolean | null
    dividende?: boolean | null
    salariati?: $Enums.DaLunarTrim | null
    casaDeMarcat?: boolean | null
    dataExpSediuSocial?: Date | string | null
    dataExpMandatAdmin?: Date | string | null
    dataCertificatFiscal?: Date | string | null
    dataFisaPlatitor?: Date | string | null
    dataVectFiscal?: Date | string | null
    createdAt?: Date | string
    updatedAt?: Date | string
    detalii?: DetaliiUncheckedCreateNestedOneWithoutClientInput
    puncteDeLucru?: PunctDeLucruUncheckedCreateNestedManyWithoutClientInput
    istorice?: IstoricUncheckedCreateNestedManyWithoutClientInput
    tasks?: TaskUncheckedCreateNestedManyWithoutClientInput
    users?: UserClientUncheckedCreateNestedManyWithoutClientInput
  }

  export type ClientUpdateInput = {
    denumire?: StringFieldUpdateOperationsInput | string
    tip?: EnumTipFieldUpdateOperationsInput | $Enums.Tip
    cui?: StringFieldUpdateOperationsInput | string
    activa?: BoolFieldUpdateOperationsInput | boolean
    dataVerificarii?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    adresa?: NullableStringFieldUpdateOperationsInput | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    impozit?: NullableEnumImpozitFieldUpdateOperationsInput | $Enums.Impozit | null
    platitorTVA?: EnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim
    tvaLaIncasare?: NullableBoolFieldUpdateOperationsInput | boolean | null
    areCodTVAUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    codTVAUE?: NullableStringFieldUpdateOperationsInput | string | null
    operatiuneUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dividende?: NullableBoolFieldUpdateOperationsInput | boolean | null
    salariati?: NullableEnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim | null
    casaDeMarcat?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dataExpSediuSocial?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataExpMandatAdmin?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataCertificatFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataFisaPlatitor?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataVectFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    createdAt?: DateTimeFieldUpdateOperationsInput | Date | string
    updatedAt?: DateTimeFieldUpdateOperationsInput | Date | string
    detalii?: DetaliiUpdateOneWithoutClientNestedInput
    puncteDeLucru?: PunctDeLucruUpdateManyWithoutClientNestedInput
    istorice?: IstoricUpdateManyWithoutClientNestedInput
    tasks?: TaskUpdateManyWithoutClientNestedInput
    users?: UserClientUpdateManyWithoutClientNestedInput
  }

  export type ClientUncheckedUpdateInput = {
    id?: IntFieldUpdateOperationsInput | number
    denumire?: StringFieldUpdateOperationsInput | string
    tip?: EnumTipFieldUpdateOperationsInput | $Enums.Tip
    cui?: StringFieldUpdateOperationsInput | string
    activa?: BoolFieldUpdateOperationsInput | boolean
    dataVerificarii?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    adresa?: NullableStringFieldUpdateOperationsInput | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    impozit?: NullableEnumImpozitFieldUpdateOperationsInput | $Enums.Impozit | null
    platitorTVA?: EnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim
    tvaLaIncasare?: NullableBoolFieldUpdateOperationsInput | boolean | null
    areCodTVAUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    codTVAUE?: NullableStringFieldUpdateOperationsInput | string | null
    operatiuneUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dividende?: NullableBoolFieldUpdateOperationsInput | boolean | null
    salariati?: NullableEnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim | null
    casaDeMarcat?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dataExpSediuSocial?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataExpMandatAdmin?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataCertificatFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataFisaPlatitor?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataVectFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    createdAt?: DateTimeFieldUpdateOperationsInput | Date | string
    updatedAt?: DateTimeFieldUpdateOperationsInput | Date | string
    detalii?: DetaliiUncheckedUpdateOneWithoutClientNestedInput
    puncteDeLucru?: PunctDeLucruUncheckedUpdateManyWithoutClientNestedInput
    istorice?: IstoricUncheckedUpdateManyWithoutClientNestedInput
    tasks?: TaskUncheckedUpdateManyWithoutClientNestedInput
    users?: UserClientUncheckedUpdateManyWithoutClientNestedInput
  }

  export type ClientCreateManyInput = {
    id?: number
    denumire: string
    tip: $Enums.Tip
    cui: string
    activa: boolean
    dataVerificarii?: Date | string | null
    adresa?: string | null
    administratie: $Enums.Administratie
    impozit?: $Enums.Impozit | null
    platitorTVA: $Enums.DaLunarTrim
    tvaLaIncasare?: boolean | null
    areCodTVAUE?: boolean | null
    codTVAUE?: string | null
    operatiuneUE?: boolean | null
    dividende?: boolean | null
    salariati?: $Enums.DaLunarTrim | null
    casaDeMarcat?: boolean | null
    dataExpSediuSocial?: Date | string | null
    dataExpMandatAdmin?: Date | string | null
    dataCertificatFiscal?: Date | string | null
    dataFisaPlatitor?: Date | string | null
    dataVectFiscal?: Date | string | null
    createdAt?: Date | string
    updatedAt?: Date | string
  }

  export type ClientUpdateManyMutationInput = {
    denumire?: StringFieldUpdateOperationsInput | string
    tip?: EnumTipFieldUpdateOperationsInput | $Enums.Tip
    cui?: StringFieldUpdateOperationsInput | string
    activa?: BoolFieldUpdateOperationsInput | boolean
    dataVerificarii?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    adresa?: NullableStringFieldUpdateOperationsInput | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    impozit?: NullableEnumImpozitFieldUpdateOperationsInput | $Enums.Impozit | null
    platitorTVA?: EnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim
    tvaLaIncasare?: NullableBoolFieldUpdateOperationsInput | boolean | null
    areCodTVAUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    codTVAUE?: NullableStringFieldUpdateOperationsInput | string | null
    operatiuneUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dividende?: NullableBoolFieldUpdateOperationsInput | boolean | null
    salariati?: NullableEnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim | null
    casaDeMarcat?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dataExpSediuSocial?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataExpMandatAdmin?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataCertificatFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataFisaPlatitor?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataVectFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    createdAt?: DateTimeFieldUpdateOperationsInput | Date | string
    updatedAt?: DateTimeFieldUpdateOperationsInput | Date | string
  }

  export type ClientUncheckedUpdateManyInput = {
    id?: IntFieldUpdateOperationsInput | number
    denumire?: StringFieldUpdateOperationsInput | string
    tip?: EnumTipFieldUpdateOperationsInput | $Enums.Tip
    cui?: StringFieldUpdateOperationsInput | string
    activa?: BoolFieldUpdateOperationsInput | boolean
    dataVerificarii?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    adresa?: NullableStringFieldUpdateOperationsInput | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    impozit?: NullableEnumImpozitFieldUpdateOperationsInput | $Enums.Impozit | null
    platitorTVA?: EnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim
    tvaLaIncasare?: NullableBoolFieldUpdateOperationsInput | boolean | null
    areCodTVAUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    codTVAUE?: NullableStringFieldUpdateOperationsInput | string | null
    operatiuneUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dividende?: NullableBoolFieldUpdateOperationsInput | boolean | null
    salariati?: NullableEnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim | null
    casaDeMarcat?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dataExpSediuSocial?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataExpMandatAdmin?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataCertificatFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataFisaPlatitor?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataVectFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    createdAt?: DateTimeFieldUpdateOperationsInput | Date | string
    updatedAt?: DateTimeFieldUpdateOperationsInput | Date | string
  }

  export type DetaliiCreateInput = {
    registruUC: boolean
    registruEvFiscala: $Enums.DaNuNuECazul
    ofSpalareBani: boolean
    regulamentOrdineInterioara: boolean
    manualPoliticiContabile: boolean
    adresaRevisal: boolean
    parolaITM?: string | null
    depunereDeclaratiiOnline: boolean
    accesDosarFiscal: $Enums.DaNuNuECazul
    client: ClientCreateNestedOneWithoutDetaliiInput
  }

  export type DetaliiUncheckedCreateInput = {
    id?: number
    registruUC: boolean
    registruEvFiscala: $Enums.DaNuNuECazul
    ofSpalareBani: boolean
    regulamentOrdineInterioara: boolean
    manualPoliticiContabile: boolean
    adresaRevisal: boolean
    parolaITM?: string | null
    depunereDeclaratiiOnline: boolean
    accesDosarFiscal: $Enums.DaNuNuECazul
    clientId: number
  }

  export type DetaliiUpdateInput = {
    registruUC?: BoolFieldUpdateOperationsInput | boolean
    registruEvFiscala?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
    ofSpalareBani?: BoolFieldUpdateOperationsInput | boolean
    regulamentOrdineInterioara?: BoolFieldUpdateOperationsInput | boolean
    manualPoliticiContabile?: BoolFieldUpdateOperationsInput | boolean
    adresaRevisal?: BoolFieldUpdateOperationsInput | boolean
    parolaITM?: NullableStringFieldUpdateOperationsInput | string | null
    depunereDeclaratiiOnline?: BoolFieldUpdateOperationsInput | boolean
    accesDosarFiscal?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
    client?: ClientUpdateOneRequiredWithoutDetaliiNestedInput
  }

  export type DetaliiUncheckedUpdateInput = {
    id?: IntFieldUpdateOperationsInput | number
    registruUC?: BoolFieldUpdateOperationsInput | boolean
    registruEvFiscala?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
    ofSpalareBani?: BoolFieldUpdateOperationsInput | boolean
    regulamentOrdineInterioara?: BoolFieldUpdateOperationsInput | boolean
    manualPoliticiContabile?: BoolFieldUpdateOperationsInput | boolean
    adresaRevisal?: BoolFieldUpdateOperationsInput | boolean
    parolaITM?: NullableStringFieldUpdateOperationsInput | string | null
    depunereDeclaratiiOnline?: BoolFieldUpdateOperationsInput | boolean
    accesDosarFiscal?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
    clientId?: IntFieldUpdateOperationsInput | number
  }

  export type DetaliiCreateManyInput = {
    id?: number
    registruUC: boolean
    registruEvFiscala: $Enums.DaNuNuECazul
    ofSpalareBani: boolean
    regulamentOrdineInterioara: boolean
    manualPoliticiContabile: boolean
    adresaRevisal: boolean
    parolaITM?: string | null
    depunereDeclaratiiOnline: boolean
    accesDosarFiscal: $Enums.DaNuNuECazul
    clientId: number
  }

  export type DetaliiUpdateManyMutationInput = {
    registruUC?: BoolFieldUpdateOperationsInput | boolean
    registruEvFiscala?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
    ofSpalareBani?: BoolFieldUpdateOperationsInput | boolean
    regulamentOrdineInterioara?: BoolFieldUpdateOperationsInput | boolean
    manualPoliticiContabile?: BoolFieldUpdateOperationsInput | boolean
    adresaRevisal?: BoolFieldUpdateOperationsInput | boolean
    parolaITM?: NullableStringFieldUpdateOperationsInput | string | null
    depunereDeclaratiiOnline?: BoolFieldUpdateOperationsInput | boolean
    accesDosarFiscal?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
  }

  export type DetaliiUncheckedUpdateManyInput = {
    id?: IntFieldUpdateOperationsInput | number
    registruUC?: BoolFieldUpdateOperationsInput | boolean
    registruEvFiscala?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
    ofSpalareBani?: BoolFieldUpdateOperationsInput | boolean
    regulamentOrdineInterioara?: BoolFieldUpdateOperationsInput | boolean
    manualPoliticiContabile?: BoolFieldUpdateOperationsInput | boolean
    adresaRevisal?: BoolFieldUpdateOperationsInput | boolean
    parolaITM?: NullableStringFieldUpdateOperationsInput | string | null
    depunereDeclaratiiOnline?: BoolFieldUpdateOperationsInput | boolean
    accesDosarFiscal?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
    clientId?: IntFieldUpdateOperationsInput | number
  }

  export type PunctDeLucruCreateInput = {
    denumire: string
    deLa: Date | string
    panaLa?: Date | string | null
    administratie: $Enums.Administratie
    registruUC: boolean
    salariati: number
    cui?: string | null
    casaDeMarcat: boolean
    client: ClientCreateNestedOneWithoutPuncteDeLucruInput
  }

  export type PunctDeLucruUncheckedCreateInput = {
    id?: number
    denumire: string
    deLa: Date | string
    panaLa?: Date | string | null
    administratie: $Enums.Administratie
    registruUC: boolean
    salariati: number
    cui?: string | null
    casaDeMarcat: boolean
    clientId: number
  }

  export type PunctDeLucruUpdateInput = {
    denumire?: StringFieldUpdateOperationsInput | string
    deLa?: DateTimeFieldUpdateOperationsInput | Date | string
    panaLa?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    registruUC?: BoolFieldUpdateOperationsInput | boolean
    salariati?: IntFieldUpdateOperationsInput | number
    cui?: NullableStringFieldUpdateOperationsInput | string | null
    casaDeMarcat?: BoolFieldUpdateOperationsInput | boolean
    client?: ClientUpdateOneRequiredWithoutPuncteDeLucruNestedInput
  }

  export type PunctDeLucruUncheckedUpdateInput = {
    id?: IntFieldUpdateOperationsInput | number
    denumire?: StringFieldUpdateOperationsInput | string
    deLa?: DateTimeFieldUpdateOperationsInput | Date | string
    panaLa?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    registruUC?: BoolFieldUpdateOperationsInput | boolean
    salariati?: IntFieldUpdateOperationsInput | number
    cui?: NullableStringFieldUpdateOperationsInput | string | null
    casaDeMarcat?: BoolFieldUpdateOperationsInput | boolean
    clientId?: IntFieldUpdateOperationsInput | number
  }

  export type PunctDeLucruCreateManyInput = {
    id?: number
    denumire: string
    deLa: Date | string
    panaLa?: Date | string | null
    administratie: $Enums.Administratie
    registruUC: boolean
    salariati: number
    cui?: string | null
    casaDeMarcat: boolean
    clientId: number
  }

  export type PunctDeLucruUpdateManyMutationInput = {
    denumire?: StringFieldUpdateOperationsInput | string
    deLa?: DateTimeFieldUpdateOperationsInput | Date | string
    panaLa?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    registruUC?: BoolFieldUpdateOperationsInput | boolean
    salariati?: IntFieldUpdateOperationsInput | number
    cui?: NullableStringFieldUpdateOperationsInput | string | null
    casaDeMarcat?: BoolFieldUpdateOperationsInput | boolean
  }

  export type PunctDeLucruUncheckedUpdateManyInput = {
    id?: IntFieldUpdateOperationsInput | number
    denumire?: StringFieldUpdateOperationsInput | string
    deLa?: DateTimeFieldUpdateOperationsInput | Date | string
    panaLa?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    registruUC?: BoolFieldUpdateOperationsInput | boolean
    salariati?: IntFieldUpdateOperationsInput | number
    cui?: NullableStringFieldUpdateOperationsInput | string | null
    casaDeMarcat?: BoolFieldUpdateOperationsInput | boolean
    clientId?: IntFieldUpdateOperationsInput | number
  }

  export type IstoricCreateInput = {
    anul: number
    cifraAfaceri: number
    inventar: boolean
    bilantSemIun: $Enums.DaNuNuECazul
    bilantAnual: $Enums.DaNuNuECazul
    client: ClientCreateNestedOneWithoutIstoriceInput
  }

  export type IstoricUncheckedCreateInput = {
    id?: number
    anul: number
    cifraAfaceri: number
    inventar: boolean
    bilantSemIun: $Enums.DaNuNuECazul
    bilantAnual: $Enums.DaNuNuECazul
    clientId: number
  }

  export type IstoricUpdateInput = {
    anul?: IntFieldUpdateOperationsInput | number
    cifraAfaceri?: FloatFieldUpdateOperationsInput | number
    inventar?: BoolFieldUpdateOperationsInput | boolean
    bilantSemIun?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
    bilantAnual?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
    client?: ClientUpdateOneRequiredWithoutIstoriceNestedInput
  }

  export type IstoricUncheckedUpdateInput = {
    id?: IntFieldUpdateOperationsInput | number
    anul?: IntFieldUpdateOperationsInput | number
    cifraAfaceri?: FloatFieldUpdateOperationsInput | number
    inventar?: BoolFieldUpdateOperationsInput | boolean
    bilantSemIun?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
    bilantAnual?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
    clientId?: IntFieldUpdateOperationsInput | number
  }

  export type IstoricCreateManyInput = {
    id?: number
    anul: number
    cifraAfaceri: number
    inventar: boolean
    bilantSemIun: $Enums.DaNuNuECazul
    bilantAnual: $Enums.DaNuNuECazul
    clientId: number
  }

  export type IstoricUpdateManyMutationInput = {
    anul?: IntFieldUpdateOperationsInput | number
    cifraAfaceri?: FloatFieldUpdateOperationsInput | number
    inventar?: BoolFieldUpdateOperationsInput | boolean
    bilantSemIun?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
    bilantAnual?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
  }

  export type IstoricUncheckedUpdateManyInput = {
    id?: IntFieldUpdateOperationsInput | number
    anul?: IntFieldUpdateOperationsInput | number
    cifraAfaceri?: FloatFieldUpdateOperationsInput | number
    inventar?: BoolFieldUpdateOperationsInput | boolean
    bilantSemIun?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
    bilantAnual?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
    clientId?: IntFieldUpdateOperationsInput | number
  }

  export type TaskCreateInput = {
    done?: boolean
    title: string
    notes?: string | null
    blocked?: string | null
    objective?: string | null
    date: Date | string
    user: UserCreateNestedOneWithoutTasksInput
    client: ClientCreateNestedOneWithoutTasksInput
  }

  export type TaskUncheckedCreateInput = {
    id?: number
    done?: boolean
    title: string
    notes?: string | null
    blocked?: string | null
    objective?: string | null
    date: Date | string
    userId: number
    clientId: number
  }

  export type TaskUpdateInput = {
    done?: BoolFieldUpdateOperationsInput | boolean
    title?: StringFieldUpdateOperationsInput | string
    notes?: NullableStringFieldUpdateOperationsInput | string | null
    blocked?: NullableStringFieldUpdateOperationsInput | string | null
    objective?: NullableStringFieldUpdateOperationsInput | string | null
    date?: DateTimeFieldUpdateOperationsInput | Date | string
    user?: UserUpdateOneRequiredWithoutTasksNestedInput
    client?: ClientUpdateOneRequiredWithoutTasksNestedInput
  }

  export type TaskUncheckedUpdateInput = {
    id?: IntFieldUpdateOperationsInput | number
    done?: BoolFieldUpdateOperationsInput | boolean
    title?: StringFieldUpdateOperationsInput | string
    notes?: NullableStringFieldUpdateOperationsInput | string | null
    blocked?: NullableStringFieldUpdateOperationsInput | string | null
    objective?: NullableStringFieldUpdateOperationsInput | string | null
    date?: DateTimeFieldUpdateOperationsInput | Date | string
    userId?: IntFieldUpdateOperationsInput | number
    clientId?: IntFieldUpdateOperationsInput | number
  }

  export type TaskCreateManyInput = {
    id?: number
    done?: boolean
    title: string
    notes?: string | null
    blocked?: string | null
    objective?: string | null
    date: Date | string
    userId: number
    clientId: number
  }

  export type TaskUpdateManyMutationInput = {
    done?: BoolFieldUpdateOperationsInput | boolean
    title?: StringFieldUpdateOperationsInput | string
    notes?: NullableStringFieldUpdateOperationsInput | string | null
    blocked?: NullableStringFieldUpdateOperationsInput | string | null
    objective?: NullableStringFieldUpdateOperationsInput | string | null
    date?: DateTimeFieldUpdateOperationsInput | Date | string
  }

  export type TaskUncheckedUpdateManyInput = {
    id?: IntFieldUpdateOperationsInput | number
    done?: BoolFieldUpdateOperationsInput | boolean
    title?: StringFieldUpdateOperationsInput | string
    notes?: NullableStringFieldUpdateOperationsInput | string | null
    blocked?: NullableStringFieldUpdateOperationsInput | string | null
    objective?: NullableStringFieldUpdateOperationsInput | string | null
    date?: DateTimeFieldUpdateOperationsInput | Date | string
    userId?: IntFieldUpdateOperationsInput | number
    clientId?: IntFieldUpdateOperationsInput | number
  }

  export type RuleCreateInput = {
    name: string
    description?: string | null
    frequency: $Enums.Frequency
    taskTitle: string
    taskNotes?: string | null
    active?: boolean
    createdAt?: Date | string
    updatedAt?: Date | string
    conditions?: RuleConditionCreateNestedManyWithoutRuleInput
  }

  export type RuleUncheckedCreateInput = {
    id?: number
    name: string
    description?: string | null
    frequency: $Enums.Frequency
    taskTitle: string
    taskNotes?: string | null
    active?: boolean
    createdAt?: Date | string
    updatedAt?: Date | string
    conditions?: RuleConditionUncheckedCreateNestedManyWithoutRuleInput
  }

  export type RuleUpdateInput = {
    name?: StringFieldUpdateOperationsInput | string
    description?: NullableStringFieldUpdateOperationsInput | string | null
    frequency?: EnumFrequencyFieldUpdateOperationsInput | $Enums.Frequency
    taskTitle?: StringFieldUpdateOperationsInput | string
    taskNotes?: NullableStringFieldUpdateOperationsInput | string | null
    active?: BoolFieldUpdateOperationsInput | boolean
    createdAt?: DateTimeFieldUpdateOperationsInput | Date | string
    updatedAt?: DateTimeFieldUpdateOperationsInput | Date | string
    conditions?: RuleConditionUpdateManyWithoutRuleNestedInput
  }

  export type RuleUncheckedUpdateInput = {
    id?: IntFieldUpdateOperationsInput | number
    name?: StringFieldUpdateOperationsInput | string
    description?: NullableStringFieldUpdateOperationsInput | string | null
    frequency?: EnumFrequencyFieldUpdateOperationsInput | $Enums.Frequency
    taskTitle?: StringFieldUpdateOperationsInput | string
    taskNotes?: NullableStringFieldUpdateOperationsInput | string | null
    active?: BoolFieldUpdateOperationsInput | boolean
    createdAt?: DateTimeFieldUpdateOperationsInput | Date | string
    updatedAt?: DateTimeFieldUpdateOperationsInput | Date | string
    conditions?: RuleConditionUncheckedUpdateManyWithoutRuleNestedInput
  }

  export type RuleCreateManyInput = {
    id?: number
    name: string
    description?: string | null
    frequency: $Enums.Frequency
    taskTitle: string
    taskNotes?: string | null
    active?: boolean
    createdAt?: Date | string
    updatedAt?: Date | string
  }

  export type RuleUpdateManyMutationInput = {
    name?: StringFieldUpdateOperationsInput | string
    description?: NullableStringFieldUpdateOperationsInput | string | null
    frequency?: EnumFrequencyFieldUpdateOperationsInput | $Enums.Frequency
    taskTitle?: StringFieldUpdateOperationsInput | string
    taskNotes?: NullableStringFieldUpdateOperationsInput | string | null
    active?: BoolFieldUpdateOperationsInput | boolean
    createdAt?: DateTimeFieldUpdateOperationsInput | Date | string
    updatedAt?: DateTimeFieldUpdateOperationsInput | Date | string
  }

  export type RuleUncheckedUpdateManyInput = {
    id?: IntFieldUpdateOperationsInput | number
    name?: StringFieldUpdateOperationsInput | string
    description?: NullableStringFieldUpdateOperationsInput | string | null
    frequency?: EnumFrequencyFieldUpdateOperationsInput | $Enums.Frequency
    taskTitle?: StringFieldUpdateOperationsInput | string
    taskNotes?: NullableStringFieldUpdateOperationsInput | string | null
    active?: BoolFieldUpdateOperationsInput | boolean
    createdAt?: DateTimeFieldUpdateOperationsInput | Date | string
    updatedAt?: DateTimeFieldUpdateOperationsInput | Date | string
  }

  export type RuleConditionCreateInput = {
    field: string
    operator?: $Enums.ConditionOperator
    value: string
    rule: RuleCreateNestedOneWithoutConditionsInput
  }

  export type RuleConditionUncheckedCreateInput = {
    id?: number
    field: string
    operator?: $Enums.ConditionOperator
    value: string
    ruleId: number
  }

  export type RuleConditionUpdateInput = {
    field?: StringFieldUpdateOperationsInput | string
    operator?: EnumConditionOperatorFieldUpdateOperationsInput | $Enums.ConditionOperator
    value?: StringFieldUpdateOperationsInput | string
    rule?: RuleUpdateOneRequiredWithoutConditionsNestedInput
  }

  export type RuleConditionUncheckedUpdateInput = {
    id?: IntFieldUpdateOperationsInput | number
    field?: StringFieldUpdateOperationsInput | string
    operator?: EnumConditionOperatorFieldUpdateOperationsInput | $Enums.ConditionOperator
    value?: StringFieldUpdateOperationsInput | string
    ruleId?: IntFieldUpdateOperationsInput | number
  }

  export type RuleConditionCreateManyInput = {
    id?: number
    field: string
    operator?: $Enums.ConditionOperator
    value: string
    ruleId: number
  }

  export type RuleConditionUpdateManyMutationInput = {
    field?: StringFieldUpdateOperationsInput | string
    operator?: EnumConditionOperatorFieldUpdateOperationsInput | $Enums.ConditionOperator
    value?: StringFieldUpdateOperationsInput | string
  }

  export type RuleConditionUncheckedUpdateManyInput = {
    id?: IntFieldUpdateOperationsInput | number
    field?: StringFieldUpdateOperationsInput | string
    operator?: EnumConditionOperatorFieldUpdateOperationsInput | $Enums.ConditionOperator
    value?: StringFieldUpdateOperationsInput | string
    ruleId?: IntFieldUpdateOperationsInput | number
  }

  export type UserClientCreateInput = {
    user: UserCreateNestedOneWithoutClientsInput
    client: ClientCreateNestedOneWithoutUsersInput
  }

  export type UserClientUncheckedCreateInput = {
    id?: number
    userId: number
    clientId: number
  }

  export type UserClientUpdateInput = {
    user?: UserUpdateOneRequiredWithoutClientsNestedInput
    client?: ClientUpdateOneRequiredWithoutUsersNestedInput
  }

  export type UserClientUncheckedUpdateInput = {
    id?: IntFieldUpdateOperationsInput | number
    userId?: IntFieldUpdateOperationsInput | number
    clientId?: IntFieldUpdateOperationsInput | number
  }

  export type UserClientCreateManyInput = {
    id?: number
    userId: number
    clientId: number
  }

  export type UserClientUpdateManyMutationInput = {

  }

  export type UserClientUncheckedUpdateManyInput = {
    id?: IntFieldUpdateOperationsInput | number
    userId?: IntFieldUpdateOperationsInput | number
    clientId?: IntFieldUpdateOperationsInput | number
  }

  export type IntFilter<$PrismaModel = never> = {
    equals?: number | IntFieldRefInput<$PrismaModel>
    in?: number[] | ListIntFieldRefInput<$PrismaModel>
    notIn?: number[] | ListIntFieldRefInput<$PrismaModel>
    lt?: number | IntFieldRefInput<$PrismaModel>
    lte?: number | IntFieldRefInput<$PrismaModel>
    gt?: number | IntFieldRefInput<$PrismaModel>
    gte?: number | IntFieldRefInput<$PrismaModel>
    not?: NestedIntFilter<$PrismaModel> | number
  }

  export type StringFilter<$PrismaModel = never> = {
    equals?: string | StringFieldRefInput<$PrismaModel>
    in?: string[] | ListStringFieldRefInput<$PrismaModel>
    notIn?: string[] | ListStringFieldRefInput<$PrismaModel>
    lt?: string | StringFieldRefInput<$PrismaModel>
    lte?: string | StringFieldRefInput<$PrismaModel>
    gt?: string | StringFieldRefInput<$PrismaModel>
    gte?: string | StringFieldRefInput<$PrismaModel>
    contains?: string | StringFieldRefInput<$PrismaModel>
    startsWith?: string | StringFieldRefInput<$PrismaModel>
    endsWith?: string | StringFieldRefInput<$PrismaModel>
    mode?: QueryMode
    not?: NestedStringFilter<$PrismaModel> | string
  }

  export type StringNullableFilter<$PrismaModel = never> = {
    equals?: string | StringFieldRefInput<$PrismaModel> | null
    in?: string[] | ListStringFieldRefInput<$PrismaModel> | null
    notIn?: string[] | ListStringFieldRefInput<$PrismaModel> | null
    lt?: string | StringFieldRefInput<$PrismaModel>
    lte?: string | StringFieldRefInput<$PrismaModel>
    gt?: string | StringFieldRefInput<$PrismaModel>
    gte?: string | StringFieldRefInput<$PrismaModel>
    contains?: string | StringFieldRefInput<$PrismaModel>
    startsWith?: string | StringFieldRefInput<$PrismaModel>
    endsWith?: string | StringFieldRefInput<$PrismaModel>
    mode?: QueryMode
    not?: NestedStringNullableFilter<$PrismaModel> | string | null
  }

  export type EnumRoleFilter<$PrismaModel = never> = {
    equals?: $Enums.Role | EnumRoleFieldRefInput<$PrismaModel>
    in?: $Enums.Role[] | ListEnumRoleFieldRefInput<$PrismaModel>
    notIn?: $Enums.Role[] | ListEnumRoleFieldRefInput<$PrismaModel>
    not?: NestedEnumRoleFilter<$PrismaModel> | $Enums.Role
  }

  export type TaskListRelationFilter = {
    every?: TaskWhereInput
    some?: TaskWhereInput
    none?: TaskWhereInput
  }

  export type UserClientListRelationFilter = {
    every?: UserClientWhereInput
    some?: UserClientWhereInput
    none?: UserClientWhereInput
  }

  export type SortOrderInput = {
    sort: SortOrder
    nulls?: NullsOrder
  }

  export type TaskOrderByRelationAggregateInput = {
    _count?: SortOrder
  }

  export type UserClientOrderByRelationAggregateInput = {
    _count?: SortOrder
  }

  export type UserCountOrderByAggregateInput = {
    id?: SortOrder
    email?: SortOrder
    name?: SortOrder
    password?: SortOrder
    rol?: SortOrder
  }

  export type UserAvgOrderByAggregateInput = {
    id?: SortOrder
  }

  export type UserMaxOrderByAggregateInput = {
    id?: SortOrder
    email?: SortOrder
    name?: SortOrder
    password?: SortOrder
    rol?: SortOrder
  }

  export type UserMinOrderByAggregateInput = {
    id?: SortOrder
    email?: SortOrder
    name?: SortOrder
    password?: SortOrder
    rol?: SortOrder
  }

  export type UserSumOrderByAggregateInput = {
    id?: SortOrder
  }

  export type IntWithAggregatesFilter<$PrismaModel = never> = {
    equals?: number | IntFieldRefInput<$PrismaModel>
    in?: number[] | ListIntFieldRefInput<$PrismaModel>
    notIn?: number[] | ListIntFieldRefInput<$PrismaModel>
    lt?: number | IntFieldRefInput<$PrismaModel>
    lte?: number | IntFieldRefInput<$PrismaModel>
    gt?: number | IntFieldRefInput<$PrismaModel>
    gte?: number | IntFieldRefInput<$PrismaModel>
    not?: NestedIntWithAggregatesFilter<$PrismaModel> | number
    _count?: NestedIntFilter<$PrismaModel>
    _avg?: NestedFloatFilter<$PrismaModel>
    _sum?: NestedIntFilter<$PrismaModel>
    _min?: NestedIntFilter<$PrismaModel>
    _max?: NestedIntFilter<$PrismaModel>
  }

  export type StringWithAggregatesFilter<$PrismaModel = never> = {
    equals?: string | StringFieldRefInput<$PrismaModel>
    in?: string[] | ListStringFieldRefInput<$PrismaModel>
    notIn?: string[] | ListStringFieldRefInput<$PrismaModel>
    lt?: string | StringFieldRefInput<$PrismaModel>
    lte?: string | StringFieldRefInput<$PrismaModel>
    gt?: string | StringFieldRefInput<$PrismaModel>
    gte?: string | StringFieldRefInput<$PrismaModel>
    contains?: string | StringFieldRefInput<$PrismaModel>
    startsWith?: string | StringFieldRefInput<$PrismaModel>
    endsWith?: string | StringFieldRefInput<$PrismaModel>
    mode?: QueryMode
    not?: NestedStringWithAggregatesFilter<$PrismaModel> | string
    _count?: NestedIntFilter<$PrismaModel>
    _min?: NestedStringFilter<$PrismaModel>
    _max?: NestedStringFilter<$PrismaModel>
  }

  export type StringNullableWithAggregatesFilter<$PrismaModel = never> = {
    equals?: string | StringFieldRefInput<$PrismaModel> | null
    in?: string[] | ListStringFieldRefInput<$PrismaModel> | null
    notIn?: string[] | ListStringFieldRefInput<$PrismaModel> | null
    lt?: string | StringFieldRefInput<$PrismaModel>
    lte?: string | StringFieldRefInput<$PrismaModel>
    gt?: string | StringFieldRefInput<$PrismaModel>
    gte?: string | StringFieldRefInput<$PrismaModel>
    contains?: string | StringFieldRefInput<$PrismaModel>
    startsWith?: string | StringFieldRefInput<$PrismaModel>
    endsWith?: string | StringFieldRefInput<$PrismaModel>
    mode?: QueryMode
    not?: NestedStringNullableWithAggregatesFilter<$PrismaModel> | string | null
    _count?: NestedIntNullableFilter<$PrismaModel>
    _min?: NestedStringNullableFilter<$PrismaModel>
    _max?: NestedStringNullableFilter<$PrismaModel>
  }

  export type EnumRoleWithAggregatesFilter<$PrismaModel = never> = {
    equals?: $Enums.Role | EnumRoleFieldRefInput<$PrismaModel>
    in?: $Enums.Role[] | ListEnumRoleFieldRefInput<$PrismaModel>
    notIn?: $Enums.Role[] | ListEnumRoleFieldRefInput<$PrismaModel>
    not?: NestedEnumRoleWithAggregatesFilter<$PrismaModel> | $Enums.Role
    _count?: NestedIntFilter<$PrismaModel>
    _min?: NestedEnumRoleFilter<$PrismaModel>
    _max?: NestedEnumRoleFilter<$PrismaModel>
  }

  export type EnumTipFilter<$PrismaModel = never> = {
    equals?: $Enums.Tip | EnumTipFieldRefInput<$PrismaModel>
    in?: $Enums.Tip[] | ListEnumTipFieldRefInput<$PrismaModel>
    notIn?: $Enums.Tip[] | ListEnumTipFieldRefInput<$PrismaModel>
    not?: NestedEnumTipFilter<$PrismaModel> | $Enums.Tip
  }

  export type BoolFilter<$PrismaModel = never> = {
    equals?: boolean | BooleanFieldRefInput<$PrismaModel>
    not?: NestedBoolFilter<$PrismaModel> | boolean
  }

  export type DateTimeNullableFilter<$PrismaModel = never> = {
    equals?: Date | string | DateTimeFieldRefInput<$PrismaModel> | null
    in?: Date[] | string[] | ListDateTimeFieldRefInput<$PrismaModel> | null
    notIn?: Date[] | string[] | ListDateTimeFieldRefInput<$PrismaModel> | null
    lt?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    lte?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    gt?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    gte?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    not?: NestedDateTimeNullableFilter<$PrismaModel> | Date | string | null
  }

  export type EnumAdministratieFilter<$PrismaModel = never> = {
    equals?: $Enums.Administratie | EnumAdministratieFieldRefInput<$PrismaModel>
    in?: $Enums.Administratie[] | ListEnumAdministratieFieldRefInput<$PrismaModel>
    notIn?: $Enums.Administratie[] | ListEnumAdministratieFieldRefInput<$PrismaModel>
    not?: NestedEnumAdministratieFilter<$PrismaModel> | $Enums.Administratie
  }

  export type EnumImpozitNullableFilter<$PrismaModel = never> = {
    equals?: $Enums.Impozit | EnumImpozitFieldRefInput<$PrismaModel> | null
    in?: $Enums.Impozit[] | ListEnumImpozitFieldRefInput<$PrismaModel> | null
    notIn?: $Enums.Impozit[] | ListEnumImpozitFieldRefInput<$PrismaModel> | null
    not?: NestedEnumImpozitNullableFilter<$PrismaModel> | $Enums.Impozit | null
  }

  export type EnumDaLunarTrimFilter<$PrismaModel = never> = {
    equals?: $Enums.DaLunarTrim | EnumDaLunarTrimFieldRefInput<$PrismaModel>
    in?: $Enums.DaLunarTrim[] | ListEnumDaLunarTrimFieldRefInput<$PrismaModel>
    notIn?: $Enums.DaLunarTrim[] | ListEnumDaLunarTrimFieldRefInput<$PrismaModel>
    not?: NestedEnumDaLunarTrimFilter<$PrismaModel> | $Enums.DaLunarTrim
  }

  export type BoolNullableFilter<$PrismaModel = never> = {
    equals?: boolean | BooleanFieldRefInput<$PrismaModel> | null
    not?: NestedBoolNullableFilter<$PrismaModel> | boolean | null
  }

  export type EnumDaLunarTrimNullableFilter<$PrismaModel = never> = {
    equals?: $Enums.DaLunarTrim | EnumDaLunarTrimFieldRefInput<$PrismaModel> | null
    in?: $Enums.DaLunarTrim[] | ListEnumDaLunarTrimFieldRefInput<$PrismaModel> | null
    notIn?: $Enums.DaLunarTrim[] | ListEnumDaLunarTrimFieldRefInput<$PrismaModel> | null
    not?: NestedEnumDaLunarTrimNullableFilter<$PrismaModel> | $Enums.DaLunarTrim | null
  }

  export type DateTimeFilter<$PrismaModel = never> = {
    equals?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    in?: Date[] | string[] | ListDateTimeFieldRefInput<$PrismaModel>
    notIn?: Date[] | string[] | ListDateTimeFieldRefInput<$PrismaModel>
    lt?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    lte?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    gt?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    gte?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    not?: NestedDateTimeFilter<$PrismaModel> | Date | string
  }

  export type DetaliiNullableScalarRelationFilter = {
    is?: DetaliiWhereInput | null
    isNot?: DetaliiWhereInput | null
  }

  export type PunctDeLucruListRelationFilter = {
    every?: PunctDeLucruWhereInput
    some?: PunctDeLucruWhereInput
    none?: PunctDeLucruWhereInput
  }

  export type IstoricListRelationFilter = {
    every?: IstoricWhereInput
    some?: IstoricWhereInput
    none?: IstoricWhereInput
  }

  export type PunctDeLucruOrderByRelationAggregateInput = {
    _count?: SortOrder
  }

  export type IstoricOrderByRelationAggregateInput = {
    _count?: SortOrder
  }

  export type ClientCountOrderByAggregateInput = {
    id?: SortOrder
    denumire?: SortOrder
    tip?: SortOrder
    cui?: SortOrder
    activa?: SortOrder
    dataVerificarii?: SortOrder
    adresa?: SortOrder
    administratie?: SortOrder
    impozit?: SortOrder
    platitorTVA?: SortOrder
    tvaLaIncasare?: SortOrder
    areCodTVAUE?: SortOrder
    codTVAUE?: SortOrder
    operatiuneUE?: SortOrder
    dividende?: SortOrder
    salariati?: SortOrder
    casaDeMarcat?: SortOrder
    dataExpSediuSocial?: SortOrder
    dataExpMandatAdmin?: SortOrder
    dataCertificatFiscal?: SortOrder
    dataFisaPlatitor?: SortOrder
    dataVectFiscal?: SortOrder
    createdAt?: SortOrder
    updatedAt?: SortOrder
  }

  export type ClientAvgOrderByAggregateInput = {
    id?: SortOrder
  }

  export type ClientMaxOrderByAggregateInput = {
    id?: SortOrder
    denumire?: SortOrder
    tip?: SortOrder
    cui?: SortOrder
    activa?: SortOrder
    dataVerificarii?: SortOrder
    adresa?: SortOrder
    administratie?: SortOrder
    impozit?: SortOrder
    platitorTVA?: SortOrder
    tvaLaIncasare?: SortOrder
    areCodTVAUE?: SortOrder
    codTVAUE?: SortOrder
    operatiuneUE?: SortOrder
    dividende?: SortOrder
    salariati?: SortOrder
    casaDeMarcat?: SortOrder
    dataExpSediuSocial?: SortOrder
    dataExpMandatAdmin?: SortOrder
    dataCertificatFiscal?: SortOrder
    dataFisaPlatitor?: SortOrder
    dataVectFiscal?: SortOrder
    createdAt?: SortOrder
    updatedAt?: SortOrder
  }

  export type ClientMinOrderByAggregateInput = {
    id?: SortOrder
    denumire?: SortOrder
    tip?: SortOrder
    cui?: SortOrder
    activa?: SortOrder
    dataVerificarii?: SortOrder
    adresa?: SortOrder
    administratie?: SortOrder
    impozit?: SortOrder
    platitorTVA?: SortOrder
    tvaLaIncasare?: SortOrder
    areCodTVAUE?: SortOrder
    codTVAUE?: SortOrder
    operatiuneUE?: SortOrder
    dividende?: SortOrder
    salariati?: SortOrder
    casaDeMarcat?: SortOrder
    dataExpSediuSocial?: SortOrder
    dataExpMandatAdmin?: SortOrder
    dataCertificatFiscal?: SortOrder
    dataFisaPlatitor?: SortOrder
    dataVectFiscal?: SortOrder
    createdAt?: SortOrder
    updatedAt?: SortOrder
  }

  export type ClientSumOrderByAggregateInput = {
    id?: SortOrder
  }

  export type EnumTipWithAggregatesFilter<$PrismaModel = never> = {
    equals?: $Enums.Tip | EnumTipFieldRefInput<$PrismaModel>
    in?: $Enums.Tip[] | ListEnumTipFieldRefInput<$PrismaModel>
    notIn?: $Enums.Tip[] | ListEnumTipFieldRefInput<$PrismaModel>
    not?: NestedEnumTipWithAggregatesFilter<$PrismaModel> | $Enums.Tip
    _count?: NestedIntFilter<$PrismaModel>
    _min?: NestedEnumTipFilter<$PrismaModel>
    _max?: NestedEnumTipFilter<$PrismaModel>
  }

  export type BoolWithAggregatesFilter<$PrismaModel = never> = {
    equals?: boolean | BooleanFieldRefInput<$PrismaModel>
    not?: NestedBoolWithAggregatesFilter<$PrismaModel> | boolean
    _count?: NestedIntFilter<$PrismaModel>
    _min?: NestedBoolFilter<$PrismaModel>
    _max?: NestedBoolFilter<$PrismaModel>
  }

  export type DateTimeNullableWithAggregatesFilter<$PrismaModel = never> = {
    equals?: Date | string | DateTimeFieldRefInput<$PrismaModel> | null
    in?: Date[] | string[] | ListDateTimeFieldRefInput<$PrismaModel> | null
    notIn?: Date[] | string[] | ListDateTimeFieldRefInput<$PrismaModel> | null
    lt?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    lte?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    gt?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    gte?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    not?: NestedDateTimeNullableWithAggregatesFilter<$PrismaModel> | Date | string | null
    _count?: NestedIntNullableFilter<$PrismaModel>
    _min?: NestedDateTimeNullableFilter<$PrismaModel>
    _max?: NestedDateTimeNullableFilter<$PrismaModel>
  }

  export type EnumAdministratieWithAggregatesFilter<$PrismaModel = never> = {
    equals?: $Enums.Administratie | EnumAdministratieFieldRefInput<$PrismaModel>
    in?: $Enums.Administratie[] | ListEnumAdministratieFieldRefInput<$PrismaModel>
    notIn?: $Enums.Administratie[] | ListEnumAdministratieFieldRefInput<$PrismaModel>
    not?: NestedEnumAdministratieWithAggregatesFilter<$PrismaModel> | $Enums.Administratie
    _count?: NestedIntFilter<$PrismaModel>
    _min?: NestedEnumAdministratieFilter<$PrismaModel>
    _max?: NestedEnumAdministratieFilter<$PrismaModel>
  }

  export type EnumImpozitNullableWithAggregatesFilter<$PrismaModel = never> = {
    equals?: $Enums.Impozit | EnumImpozitFieldRefInput<$PrismaModel> | null
    in?: $Enums.Impozit[] | ListEnumImpozitFieldRefInput<$PrismaModel> | null
    notIn?: $Enums.Impozit[] | ListEnumImpozitFieldRefInput<$PrismaModel> | null
    not?: NestedEnumImpozitNullableWithAggregatesFilter<$PrismaModel> | $Enums.Impozit | null
    _count?: NestedIntNullableFilter<$PrismaModel>
    _min?: NestedEnumImpozitNullableFilter<$PrismaModel>
    _max?: NestedEnumImpozitNullableFilter<$PrismaModel>
  }

  export type EnumDaLunarTrimWithAggregatesFilter<$PrismaModel = never> = {
    equals?: $Enums.DaLunarTrim | EnumDaLunarTrimFieldRefInput<$PrismaModel>
    in?: $Enums.DaLunarTrim[] | ListEnumDaLunarTrimFieldRefInput<$PrismaModel>
    notIn?: $Enums.DaLunarTrim[] | ListEnumDaLunarTrimFieldRefInput<$PrismaModel>
    not?: NestedEnumDaLunarTrimWithAggregatesFilter<$PrismaModel> | $Enums.DaLunarTrim
    _count?: NestedIntFilter<$PrismaModel>
    _min?: NestedEnumDaLunarTrimFilter<$PrismaModel>
    _max?: NestedEnumDaLunarTrimFilter<$PrismaModel>
  }

  export type BoolNullableWithAggregatesFilter<$PrismaModel = never> = {
    equals?: boolean | BooleanFieldRefInput<$PrismaModel> | null
    not?: NestedBoolNullableWithAggregatesFilter<$PrismaModel> | boolean | null
    _count?: NestedIntNullableFilter<$PrismaModel>
    _min?: NestedBoolNullableFilter<$PrismaModel>
    _max?: NestedBoolNullableFilter<$PrismaModel>
  }

  export type EnumDaLunarTrimNullableWithAggregatesFilter<$PrismaModel = never> = {
    equals?: $Enums.DaLunarTrim | EnumDaLunarTrimFieldRefInput<$PrismaModel> | null
    in?: $Enums.DaLunarTrim[] | ListEnumDaLunarTrimFieldRefInput<$PrismaModel> | null
    notIn?: $Enums.DaLunarTrim[] | ListEnumDaLunarTrimFieldRefInput<$PrismaModel> | null
    not?: NestedEnumDaLunarTrimNullableWithAggregatesFilter<$PrismaModel> | $Enums.DaLunarTrim | null
    _count?: NestedIntNullableFilter<$PrismaModel>
    _min?: NestedEnumDaLunarTrimNullableFilter<$PrismaModel>
    _max?: NestedEnumDaLunarTrimNullableFilter<$PrismaModel>
  }

  export type DateTimeWithAggregatesFilter<$PrismaModel = never> = {
    equals?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    in?: Date[] | string[] | ListDateTimeFieldRefInput<$PrismaModel>
    notIn?: Date[] | string[] | ListDateTimeFieldRefInput<$PrismaModel>
    lt?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    lte?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    gt?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    gte?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    not?: NestedDateTimeWithAggregatesFilter<$PrismaModel> | Date | string
    _count?: NestedIntFilter<$PrismaModel>
    _min?: NestedDateTimeFilter<$PrismaModel>
    _max?: NestedDateTimeFilter<$PrismaModel>
  }

  export type EnumDaNuNuECazulFilter<$PrismaModel = never> = {
    equals?: $Enums.DaNuNuECazul | EnumDaNuNuECazulFieldRefInput<$PrismaModel>
    in?: $Enums.DaNuNuECazul[] | ListEnumDaNuNuECazulFieldRefInput<$PrismaModel>
    notIn?: $Enums.DaNuNuECazul[] | ListEnumDaNuNuECazulFieldRefInput<$PrismaModel>
    not?: NestedEnumDaNuNuECazulFilter<$PrismaModel> | $Enums.DaNuNuECazul
  }

  export type ClientScalarRelationFilter = {
    is?: ClientWhereInput
    isNot?: ClientWhereInput
  }

  export type DetaliiCountOrderByAggregateInput = {
    id?: SortOrder
    registruUC?: SortOrder
    registruEvFiscala?: SortOrder
    ofSpalareBani?: SortOrder
    regulamentOrdineInterioara?: SortOrder
    manualPoliticiContabile?: SortOrder
    adresaRevisal?: SortOrder
    parolaITM?: SortOrder
    depunereDeclaratiiOnline?: SortOrder
    accesDosarFiscal?: SortOrder
    clientId?: SortOrder
  }

  export type DetaliiAvgOrderByAggregateInput = {
    id?: SortOrder
    clientId?: SortOrder
  }

  export type DetaliiMaxOrderByAggregateInput = {
    id?: SortOrder
    registruUC?: SortOrder
    registruEvFiscala?: SortOrder
    ofSpalareBani?: SortOrder
    regulamentOrdineInterioara?: SortOrder
    manualPoliticiContabile?: SortOrder
    adresaRevisal?: SortOrder
    parolaITM?: SortOrder
    depunereDeclaratiiOnline?: SortOrder
    accesDosarFiscal?: SortOrder
    clientId?: SortOrder
  }

  export type DetaliiMinOrderByAggregateInput = {
    id?: SortOrder
    registruUC?: SortOrder
    registruEvFiscala?: SortOrder
    ofSpalareBani?: SortOrder
    regulamentOrdineInterioara?: SortOrder
    manualPoliticiContabile?: SortOrder
    adresaRevisal?: SortOrder
    parolaITM?: SortOrder
    depunereDeclaratiiOnline?: SortOrder
    accesDosarFiscal?: SortOrder
    clientId?: SortOrder
  }

  export type DetaliiSumOrderByAggregateInput = {
    id?: SortOrder
    clientId?: SortOrder
  }

  export type EnumDaNuNuECazulWithAggregatesFilter<$PrismaModel = never> = {
    equals?: $Enums.DaNuNuECazul | EnumDaNuNuECazulFieldRefInput<$PrismaModel>
    in?: $Enums.DaNuNuECazul[] | ListEnumDaNuNuECazulFieldRefInput<$PrismaModel>
    notIn?: $Enums.DaNuNuECazul[] | ListEnumDaNuNuECazulFieldRefInput<$PrismaModel>
    not?: NestedEnumDaNuNuECazulWithAggregatesFilter<$PrismaModel> | $Enums.DaNuNuECazul
    _count?: NestedIntFilter<$PrismaModel>
    _min?: NestedEnumDaNuNuECazulFilter<$PrismaModel>
    _max?: NestedEnumDaNuNuECazulFilter<$PrismaModel>
  }

  export type PunctDeLucruCountOrderByAggregateInput = {
    id?: SortOrder
    denumire?: SortOrder
    deLa?: SortOrder
    panaLa?: SortOrder
    administratie?: SortOrder
    registruUC?: SortOrder
    salariati?: SortOrder
    cui?: SortOrder
    casaDeMarcat?: SortOrder
    clientId?: SortOrder
  }

  export type PunctDeLucruAvgOrderByAggregateInput = {
    id?: SortOrder
    salariati?: SortOrder
    clientId?: SortOrder
  }

  export type PunctDeLucruMaxOrderByAggregateInput = {
    id?: SortOrder
    denumire?: SortOrder
    deLa?: SortOrder
    panaLa?: SortOrder
    administratie?: SortOrder
    registruUC?: SortOrder
    salariati?: SortOrder
    cui?: SortOrder
    casaDeMarcat?: SortOrder
    clientId?: SortOrder
  }

  export type PunctDeLucruMinOrderByAggregateInput = {
    id?: SortOrder
    denumire?: SortOrder
    deLa?: SortOrder
    panaLa?: SortOrder
    administratie?: SortOrder
    registruUC?: SortOrder
    salariati?: SortOrder
    cui?: SortOrder
    casaDeMarcat?: SortOrder
    clientId?: SortOrder
  }

  export type PunctDeLucruSumOrderByAggregateInput = {
    id?: SortOrder
    salariati?: SortOrder
    clientId?: SortOrder
  }

  export type FloatFilter<$PrismaModel = never> = {
    equals?: number | FloatFieldRefInput<$PrismaModel>
    in?: number[] | ListFloatFieldRefInput<$PrismaModel>
    notIn?: number[] | ListFloatFieldRefInput<$PrismaModel>
    lt?: number | FloatFieldRefInput<$PrismaModel>
    lte?: number | FloatFieldRefInput<$PrismaModel>
    gt?: number | FloatFieldRefInput<$PrismaModel>
    gte?: number | FloatFieldRefInput<$PrismaModel>
    not?: NestedFloatFilter<$PrismaModel> | number
  }

  export type IstoricClientIdAnulCompoundUniqueInput = {
    clientId: number
    anul: number
  }

  export type IstoricCountOrderByAggregateInput = {
    id?: SortOrder
    anul?: SortOrder
    cifraAfaceri?: SortOrder
    inventar?: SortOrder
    bilantSemIun?: SortOrder
    bilantAnual?: SortOrder
    clientId?: SortOrder
  }

  export type IstoricAvgOrderByAggregateInput = {
    id?: SortOrder
    anul?: SortOrder
    cifraAfaceri?: SortOrder
    clientId?: SortOrder
  }

  export type IstoricMaxOrderByAggregateInput = {
    id?: SortOrder
    anul?: SortOrder
    cifraAfaceri?: SortOrder
    inventar?: SortOrder
    bilantSemIun?: SortOrder
    bilantAnual?: SortOrder
    clientId?: SortOrder
  }

  export type IstoricMinOrderByAggregateInput = {
    id?: SortOrder
    anul?: SortOrder
    cifraAfaceri?: SortOrder
    inventar?: SortOrder
    bilantSemIun?: SortOrder
    bilantAnual?: SortOrder
    clientId?: SortOrder
  }

  export type IstoricSumOrderByAggregateInput = {
    id?: SortOrder
    anul?: SortOrder
    cifraAfaceri?: SortOrder
    clientId?: SortOrder
  }

  export type FloatWithAggregatesFilter<$PrismaModel = never> = {
    equals?: number | FloatFieldRefInput<$PrismaModel>
    in?: number[] | ListFloatFieldRefInput<$PrismaModel>
    notIn?: number[] | ListFloatFieldRefInput<$PrismaModel>
    lt?: number | FloatFieldRefInput<$PrismaModel>
    lte?: number | FloatFieldRefInput<$PrismaModel>
    gt?: number | FloatFieldRefInput<$PrismaModel>
    gte?: number | FloatFieldRefInput<$PrismaModel>
    not?: NestedFloatWithAggregatesFilter<$PrismaModel> | number
    _count?: NestedIntFilter<$PrismaModel>
    _avg?: NestedFloatFilter<$PrismaModel>
    _sum?: NestedFloatFilter<$PrismaModel>
    _min?: NestedFloatFilter<$PrismaModel>
    _max?: NestedFloatFilter<$PrismaModel>
  }

  export type UserScalarRelationFilter = {
    is?: UserWhereInput
    isNot?: UserWhereInput
  }

  export type TaskCountOrderByAggregateInput = {
    id?: SortOrder
    done?: SortOrder
    title?: SortOrder
    notes?: SortOrder
    blocked?: SortOrder
    objective?: SortOrder
    date?: SortOrder
    userId?: SortOrder
    clientId?: SortOrder
  }

  export type TaskAvgOrderByAggregateInput = {
    id?: SortOrder
    userId?: SortOrder
    clientId?: SortOrder
  }

  export type TaskMaxOrderByAggregateInput = {
    id?: SortOrder
    done?: SortOrder
    title?: SortOrder
    notes?: SortOrder
    blocked?: SortOrder
    objective?: SortOrder
    date?: SortOrder
    userId?: SortOrder
    clientId?: SortOrder
  }

  export type TaskMinOrderByAggregateInput = {
    id?: SortOrder
    done?: SortOrder
    title?: SortOrder
    notes?: SortOrder
    blocked?: SortOrder
    objective?: SortOrder
    date?: SortOrder
    userId?: SortOrder
    clientId?: SortOrder
  }

  export type TaskSumOrderByAggregateInput = {
    id?: SortOrder
    userId?: SortOrder
    clientId?: SortOrder
  }

  export type EnumFrequencyFilter<$PrismaModel = never> = {
    equals?: $Enums.Frequency | EnumFrequencyFieldRefInput<$PrismaModel>
    in?: $Enums.Frequency[] | ListEnumFrequencyFieldRefInput<$PrismaModel>
    notIn?: $Enums.Frequency[] | ListEnumFrequencyFieldRefInput<$PrismaModel>
    not?: NestedEnumFrequencyFilter<$PrismaModel> | $Enums.Frequency
  }

  export type RuleConditionListRelationFilter = {
    every?: RuleConditionWhereInput
    some?: RuleConditionWhereInput
    none?: RuleConditionWhereInput
  }

  export type RuleConditionOrderByRelationAggregateInput = {
    _count?: SortOrder
  }

  export type RuleCountOrderByAggregateInput = {
    id?: SortOrder
    name?: SortOrder
    description?: SortOrder
    frequency?: SortOrder
    taskTitle?: SortOrder
    taskNotes?: SortOrder
    active?: SortOrder
    createdAt?: SortOrder
    updatedAt?: SortOrder
  }

  export type RuleAvgOrderByAggregateInput = {
    id?: SortOrder
  }

  export type RuleMaxOrderByAggregateInput = {
    id?: SortOrder
    name?: SortOrder
    description?: SortOrder
    frequency?: SortOrder
    taskTitle?: SortOrder
    taskNotes?: SortOrder
    active?: SortOrder
    createdAt?: SortOrder
    updatedAt?: SortOrder
  }

  export type RuleMinOrderByAggregateInput = {
    id?: SortOrder
    name?: SortOrder
    description?: SortOrder
    frequency?: SortOrder
    taskTitle?: SortOrder
    taskNotes?: SortOrder
    active?: SortOrder
    createdAt?: SortOrder
    updatedAt?: SortOrder
  }

  export type RuleSumOrderByAggregateInput = {
    id?: SortOrder
  }

  export type EnumFrequencyWithAggregatesFilter<$PrismaModel = never> = {
    equals?: $Enums.Frequency | EnumFrequencyFieldRefInput<$PrismaModel>
    in?: $Enums.Frequency[] | ListEnumFrequencyFieldRefInput<$PrismaModel>
    notIn?: $Enums.Frequency[] | ListEnumFrequencyFieldRefInput<$PrismaModel>
    not?: NestedEnumFrequencyWithAggregatesFilter<$PrismaModel> | $Enums.Frequency
    _count?: NestedIntFilter<$PrismaModel>
    _min?: NestedEnumFrequencyFilter<$PrismaModel>
    _max?: NestedEnumFrequencyFilter<$PrismaModel>
  }

  export type EnumConditionOperatorFilter<$PrismaModel = never> = {
    equals?: $Enums.ConditionOperator | EnumConditionOperatorFieldRefInput<$PrismaModel>
    in?: $Enums.ConditionOperator[] | ListEnumConditionOperatorFieldRefInput<$PrismaModel>
    notIn?: $Enums.ConditionOperator[] | ListEnumConditionOperatorFieldRefInput<$PrismaModel>
    not?: NestedEnumConditionOperatorFilter<$PrismaModel> | $Enums.ConditionOperator
  }

  export type RuleScalarRelationFilter = {
    is?: RuleWhereInput
    isNot?: RuleWhereInput
  }

  export type RuleConditionCountOrderByAggregateInput = {
    id?: SortOrder
    field?: SortOrder
    operator?: SortOrder
    value?: SortOrder
    ruleId?: SortOrder
  }

  export type RuleConditionAvgOrderByAggregateInput = {
    id?: SortOrder
    ruleId?: SortOrder
  }

  export type RuleConditionMaxOrderByAggregateInput = {
    id?: SortOrder
    field?: SortOrder
    operator?: SortOrder
    value?: SortOrder
    ruleId?: SortOrder
  }

  export type RuleConditionMinOrderByAggregateInput = {
    id?: SortOrder
    field?: SortOrder
    operator?: SortOrder
    value?: SortOrder
    ruleId?: SortOrder
  }

  export type RuleConditionSumOrderByAggregateInput = {
    id?: SortOrder
    ruleId?: SortOrder
  }

  export type EnumConditionOperatorWithAggregatesFilter<$PrismaModel = never> = {
    equals?: $Enums.ConditionOperator | EnumConditionOperatorFieldRefInput<$PrismaModel>
    in?: $Enums.ConditionOperator[] | ListEnumConditionOperatorFieldRefInput<$PrismaModel>
    notIn?: $Enums.ConditionOperator[] | ListEnumConditionOperatorFieldRefInput<$PrismaModel>
    not?: NestedEnumConditionOperatorWithAggregatesFilter<$PrismaModel> | $Enums.ConditionOperator
    _count?: NestedIntFilter<$PrismaModel>
    _min?: NestedEnumConditionOperatorFilter<$PrismaModel>
    _max?: NestedEnumConditionOperatorFilter<$PrismaModel>
  }

  export type UserClientUserIdClientIdCompoundUniqueInput = {
    userId: number
    clientId: number
  }

  export type UserClientCountOrderByAggregateInput = {
    id?: SortOrder
    userId?: SortOrder
    clientId?: SortOrder
  }

  export type UserClientAvgOrderByAggregateInput = {
    id?: SortOrder
    userId?: SortOrder
    clientId?: SortOrder
  }

  export type UserClientMaxOrderByAggregateInput = {
    id?: SortOrder
    userId?: SortOrder
    clientId?: SortOrder
  }

  export type UserClientMinOrderByAggregateInput = {
    id?: SortOrder
    userId?: SortOrder
    clientId?: SortOrder
  }

  export type UserClientSumOrderByAggregateInput = {
    id?: SortOrder
    userId?: SortOrder
    clientId?: SortOrder
  }

  export type TaskCreateNestedManyWithoutUserInput = {
    create?: XOR<TaskCreateWithoutUserInput, TaskUncheckedCreateWithoutUserInput> | TaskCreateWithoutUserInput[] | TaskUncheckedCreateWithoutUserInput[]
    connectOrCreate?: TaskCreateOrConnectWithoutUserInput | TaskCreateOrConnectWithoutUserInput[]
    createMany?: TaskCreateManyUserInputEnvelope
    connect?: TaskWhereUniqueInput | TaskWhereUniqueInput[]
  }

  export type UserClientCreateNestedManyWithoutUserInput = {
    create?: XOR<UserClientCreateWithoutUserInput, UserClientUncheckedCreateWithoutUserInput> | UserClientCreateWithoutUserInput[] | UserClientUncheckedCreateWithoutUserInput[]
    connectOrCreate?: UserClientCreateOrConnectWithoutUserInput | UserClientCreateOrConnectWithoutUserInput[]
    createMany?: UserClientCreateManyUserInputEnvelope
    connect?: UserClientWhereUniqueInput | UserClientWhereUniqueInput[]
  }

  export type TaskUncheckedCreateNestedManyWithoutUserInput = {
    create?: XOR<TaskCreateWithoutUserInput, TaskUncheckedCreateWithoutUserInput> | TaskCreateWithoutUserInput[] | TaskUncheckedCreateWithoutUserInput[]
    connectOrCreate?: TaskCreateOrConnectWithoutUserInput | TaskCreateOrConnectWithoutUserInput[]
    createMany?: TaskCreateManyUserInputEnvelope
    connect?: TaskWhereUniqueInput | TaskWhereUniqueInput[]
  }

  export type UserClientUncheckedCreateNestedManyWithoutUserInput = {
    create?: XOR<UserClientCreateWithoutUserInput, UserClientUncheckedCreateWithoutUserInput> | UserClientCreateWithoutUserInput[] | UserClientUncheckedCreateWithoutUserInput[]
    connectOrCreate?: UserClientCreateOrConnectWithoutUserInput | UserClientCreateOrConnectWithoutUserInput[]
    createMany?: UserClientCreateManyUserInputEnvelope
    connect?: UserClientWhereUniqueInput | UserClientWhereUniqueInput[]
  }

  export type StringFieldUpdateOperationsInput = {
    set?: string
  }

  export type NullableStringFieldUpdateOperationsInput = {
    set?: string | null
  }

  export type EnumRoleFieldUpdateOperationsInput = {
    set?: $Enums.Role
  }

  export type TaskUpdateManyWithoutUserNestedInput = {
    create?: XOR<TaskCreateWithoutUserInput, TaskUncheckedCreateWithoutUserInput> | TaskCreateWithoutUserInput[] | TaskUncheckedCreateWithoutUserInput[]
    connectOrCreate?: TaskCreateOrConnectWithoutUserInput | TaskCreateOrConnectWithoutUserInput[]
    upsert?: TaskUpsertWithWhereUniqueWithoutUserInput | TaskUpsertWithWhereUniqueWithoutUserInput[]
    createMany?: TaskCreateManyUserInputEnvelope
    set?: TaskWhereUniqueInput | TaskWhereUniqueInput[]
    disconnect?: TaskWhereUniqueInput | TaskWhereUniqueInput[]
    delete?: TaskWhereUniqueInput | TaskWhereUniqueInput[]
    connect?: TaskWhereUniqueInput | TaskWhereUniqueInput[]
    update?: TaskUpdateWithWhereUniqueWithoutUserInput | TaskUpdateWithWhereUniqueWithoutUserInput[]
    updateMany?: TaskUpdateManyWithWhereWithoutUserInput | TaskUpdateManyWithWhereWithoutUserInput[]
    deleteMany?: TaskScalarWhereInput | TaskScalarWhereInput[]
  }

  export type UserClientUpdateManyWithoutUserNestedInput = {
    create?: XOR<UserClientCreateWithoutUserInput, UserClientUncheckedCreateWithoutUserInput> | UserClientCreateWithoutUserInput[] | UserClientUncheckedCreateWithoutUserInput[]
    connectOrCreate?: UserClientCreateOrConnectWithoutUserInput | UserClientCreateOrConnectWithoutUserInput[]
    upsert?: UserClientUpsertWithWhereUniqueWithoutUserInput | UserClientUpsertWithWhereUniqueWithoutUserInput[]
    createMany?: UserClientCreateManyUserInputEnvelope
    set?: UserClientWhereUniqueInput | UserClientWhereUniqueInput[]
    disconnect?: UserClientWhereUniqueInput | UserClientWhereUniqueInput[]
    delete?: UserClientWhereUniqueInput | UserClientWhereUniqueInput[]
    connect?: UserClientWhereUniqueInput | UserClientWhereUniqueInput[]
    update?: UserClientUpdateWithWhereUniqueWithoutUserInput | UserClientUpdateWithWhereUniqueWithoutUserInput[]
    updateMany?: UserClientUpdateManyWithWhereWithoutUserInput | UserClientUpdateManyWithWhereWithoutUserInput[]
    deleteMany?: UserClientScalarWhereInput | UserClientScalarWhereInput[]
  }

  export type IntFieldUpdateOperationsInput = {
    set?: number
    increment?: number
    decrement?: number
    multiply?: number
    divide?: number
  }

  export type TaskUncheckedUpdateManyWithoutUserNestedInput = {
    create?: XOR<TaskCreateWithoutUserInput, TaskUncheckedCreateWithoutUserInput> | TaskCreateWithoutUserInput[] | TaskUncheckedCreateWithoutUserInput[]
    connectOrCreate?: TaskCreateOrConnectWithoutUserInput | TaskCreateOrConnectWithoutUserInput[]
    upsert?: TaskUpsertWithWhereUniqueWithoutUserInput | TaskUpsertWithWhereUniqueWithoutUserInput[]
    createMany?: TaskCreateManyUserInputEnvelope
    set?: TaskWhereUniqueInput | TaskWhereUniqueInput[]
    disconnect?: TaskWhereUniqueInput | TaskWhereUniqueInput[]
    delete?: TaskWhereUniqueInput | TaskWhereUniqueInput[]
    connect?: TaskWhereUniqueInput | TaskWhereUniqueInput[]
    update?: TaskUpdateWithWhereUniqueWithoutUserInput | TaskUpdateWithWhereUniqueWithoutUserInput[]
    updateMany?: TaskUpdateManyWithWhereWithoutUserInput | TaskUpdateManyWithWhereWithoutUserInput[]
    deleteMany?: TaskScalarWhereInput | TaskScalarWhereInput[]
  }

  export type UserClientUncheckedUpdateManyWithoutUserNestedInput = {
    create?: XOR<UserClientCreateWithoutUserInput, UserClientUncheckedCreateWithoutUserInput> | UserClientCreateWithoutUserInput[] | UserClientUncheckedCreateWithoutUserInput[]
    connectOrCreate?: UserClientCreateOrConnectWithoutUserInput | UserClientCreateOrConnectWithoutUserInput[]
    upsert?: UserClientUpsertWithWhereUniqueWithoutUserInput | UserClientUpsertWithWhereUniqueWithoutUserInput[]
    createMany?: UserClientCreateManyUserInputEnvelope
    set?: UserClientWhereUniqueInput | UserClientWhereUniqueInput[]
    disconnect?: UserClientWhereUniqueInput | UserClientWhereUniqueInput[]
    delete?: UserClientWhereUniqueInput | UserClientWhereUniqueInput[]
    connect?: UserClientWhereUniqueInput | UserClientWhereUniqueInput[]
    update?: UserClientUpdateWithWhereUniqueWithoutUserInput | UserClientUpdateWithWhereUniqueWithoutUserInput[]
    updateMany?: UserClientUpdateManyWithWhereWithoutUserInput | UserClientUpdateManyWithWhereWithoutUserInput[]
    deleteMany?: UserClientScalarWhereInput | UserClientScalarWhereInput[]
  }

  export type DetaliiCreateNestedOneWithoutClientInput = {
    create?: XOR<DetaliiCreateWithoutClientInput, DetaliiUncheckedCreateWithoutClientInput>
    connectOrCreate?: DetaliiCreateOrConnectWithoutClientInput
    connect?: DetaliiWhereUniqueInput
  }

  export type PunctDeLucruCreateNestedManyWithoutClientInput = {
    create?: XOR<PunctDeLucruCreateWithoutClientInput, PunctDeLucruUncheckedCreateWithoutClientInput> | PunctDeLucruCreateWithoutClientInput[] | PunctDeLucruUncheckedCreateWithoutClientInput[]
    connectOrCreate?: PunctDeLucruCreateOrConnectWithoutClientInput | PunctDeLucruCreateOrConnectWithoutClientInput[]
    createMany?: PunctDeLucruCreateManyClientInputEnvelope
    connect?: PunctDeLucruWhereUniqueInput | PunctDeLucruWhereUniqueInput[]
  }

  export type IstoricCreateNestedManyWithoutClientInput = {
    create?: XOR<IstoricCreateWithoutClientInput, IstoricUncheckedCreateWithoutClientInput> | IstoricCreateWithoutClientInput[] | IstoricUncheckedCreateWithoutClientInput[]
    connectOrCreate?: IstoricCreateOrConnectWithoutClientInput | IstoricCreateOrConnectWithoutClientInput[]
    createMany?: IstoricCreateManyClientInputEnvelope
    connect?: IstoricWhereUniqueInput | IstoricWhereUniqueInput[]
  }

  export type TaskCreateNestedManyWithoutClientInput = {
    create?: XOR<TaskCreateWithoutClientInput, TaskUncheckedCreateWithoutClientInput> | TaskCreateWithoutClientInput[] | TaskUncheckedCreateWithoutClientInput[]
    connectOrCreate?: TaskCreateOrConnectWithoutClientInput | TaskCreateOrConnectWithoutClientInput[]
    createMany?: TaskCreateManyClientInputEnvelope
    connect?: TaskWhereUniqueInput | TaskWhereUniqueInput[]
  }

  export type UserClientCreateNestedManyWithoutClientInput = {
    create?: XOR<UserClientCreateWithoutClientInput, UserClientUncheckedCreateWithoutClientInput> | UserClientCreateWithoutClientInput[] | UserClientUncheckedCreateWithoutClientInput[]
    connectOrCreate?: UserClientCreateOrConnectWithoutClientInput | UserClientCreateOrConnectWithoutClientInput[]
    createMany?: UserClientCreateManyClientInputEnvelope
    connect?: UserClientWhereUniqueInput | UserClientWhereUniqueInput[]
  }

  export type DetaliiUncheckedCreateNestedOneWithoutClientInput = {
    create?: XOR<DetaliiCreateWithoutClientInput, DetaliiUncheckedCreateWithoutClientInput>
    connectOrCreate?: DetaliiCreateOrConnectWithoutClientInput
    connect?: DetaliiWhereUniqueInput
  }

  export type PunctDeLucruUncheckedCreateNestedManyWithoutClientInput = {
    create?: XOR<PunctDeLucruCreateWithoutClientInput, PunctDeLucruUncheckedCreateWithoutClientInput> | PunctDeLucruCreateWithoutClientInput[] | PunctDeLucruUncheckedCreateWithoutClientInput[]
    connectOrCreate?: PunctDeLucruCreateOrConnectWithoutClientInput | PunctDeLucruCreateOrConnectWithoutClientInput[]
    createMany?: PunctDeLucruCreateManyClientInputEnvelope
    connect?: PunctDeLucruWhereUniqueInput | PunctDeLucruWhereUniqueInput[]
  }

  export type IstoricUncheckedCreateNestedManyWithoutClientInput = {
    create?: XOR<IstoricCreateWithoutClientInput, IstoricUncheckedCreateWithoutClientInput> | IstoricCreateWithoutClientInput[] | IstoricUncheckedCreateWithoutClientInput[]
    connectOrCreate?: IstoricCreateOrConnectWithoutClientInput | IstoricCreateOrConnectWithoutClientInput[]
    createMany?: IstoricCreateManyClientInputEnvelope
    connect?: IstoricWhereUniqueInput | IstoricWhereUniqueInput[]
  }

  export type TaskUncheckedCreateNestedManyWithoutClientInput = {
    create?: XOR<TaskCreateWithoutClientInput, TaskUncheckedCreateWithoutClientInput> | TaskCreateWithoutClientInput[] | TaskUncheckedCreateWithoutClientInput[]
    connectOrCreate?: TaskCreateOrConnectWithoutClientInput | TaskCreateOrConnectWithoutClientInput[]
    createMany?: TaskCreateManyClientInputEnvelope
    connect?: TaskWhereUniqueInput | TaskWhereUniqueInput[]
  }

  export type UserClientUncheckedCreateNestedManyWithoutClientInput = {
    create?: XOR<UserClientCreateWithoutClientInput, UserClientUncheckedCreateWithoutClientInput> | UserClientCreateWithoutClientInput[] | UserClientUncheckedCreateWithoutClientInput[]
    connectOrCreate?: UserClientCreateOrConnectWithoutClientInput | UserClientCreateOrConnectWithoutClientInput[]
    createMany?: UserClientCreateManyClientInputEnvelope
    connect?: UserClientWhereUniqueInput | UserClientWhereUniqueInput[]
  }

  export type EnumTipFieldUpdateOperationsInput = {
    set?: $Enums.Tip
  }

  export type BoolFieldUpdateOperationsInput = {
    set?: boolean
  }

  export type NullableDateTimeFieldUpdateOperationsInput = {
    set?: Date | string | null
  }

  export type EnumAdministratieFieldUpdateOperationsInput = {
    set?: $Enums.Administratie
  }

  export type NullableEnumImpozitFieldUpdateOperationsInput = {
    set?: $Enums.Impozit | null
  }

  export type EnumDaLunarTrimFieldUpdateOperationsInput = {
    set?: $Enums.DaLunarTrim
  }

  export type NullableBoolFieldUpdateOperationsInput = {
    set?: boolean | null
  }

  export type NullableEnumDaLunarTrimFieldUpdateOperationsInput = {
    set?: $Enums.DaLunarTrim | null
  }

  export type DateTimeFieldUpdateOperationsInput = {
    set?: Date | string
  }

  export type DetaliiUpdateOneWithoutClientNestedInput = {
    create?: XOR<DetaliiCreateWithoutClientInput, DetaliiUncheckedCreateWithoutClientInput>
    connectOrCreate?: DetaliiCreateOrConnectWithoutClientInput
    upsert?: DetaliiUpsertWithoutClientInput
    disconnect?: DetaliiWhereInput | boolean
    delete?: DetaliiWhereInput | boolean
    connect?: DetaliiWhereUniqueInput
    update?: XOR<XOR<DetaliiUpdateToOneWithWhereWithoutClientInput, DetaliiUpdateWithoutClientInput>, DetaliiUncheckedUpdateWithoutClientInput>
  }

  export type PunctDeLucruUpdateManyWithoutClientNestedInput = {
    create?: XOR<PunctDeLucruCreateWithoutClientInput, PunctDeLucruUncheckedCreateWithoutClientInput> | PunctDeLucruCreateWithoutClientInput[] | PunctDeLucruUncheckedCreateWithoutClientInput[]
    connectOrCreate?: PunctDeLucruCreateOrConnectWithoutClientInput | PunctDeLucruCreateOrConnectWithoutClientInput[]
    upsert?: PunctDeLucruUpsertWithWhereUniqueWithoutClientInput | PunctDeLucruUpsertWithWhereUniqueWithoutClientInput[]
    createMany?: PunctDeLucruCreateManyClientInputEnvelope
    set?: PunctDeLucruWhereUniqueInput | PunctDeLucruWhereUniqueInput[]
    disconnect?: PunctDeLucruWhereUniqueInput | PunctDeLucruWhereUniqueInput[]
    delete?: PunctDeLucruWhereUniqueInput | PunctDeLucruWhereUniqueInput[]
    connect?: PunctDeLucruWhereUniqueInput | PunctDeLucruWhereUniqueInput[]
    update?: PunctDeLucruUpdateWithWhereUniqueWithoutClientInput | PunctDeLucruUpdateWithWhereUniqueWithoutClientInput[]
    updateMany?: PunctDeLucruUpdateManyWithWhereWithoutClientInput | PunctDeLucruUpdateManyWithWhereWithoutClientInput[]
    deleteMany?: PunctDeLucruScalarWhereInput | PunctDeLucruScalarWhereInput[]
  }

  export type IstoricUpdateManyWithoutClientNestedInput = {
    create?: XOR<IstoricCreateWithoutClientInput, IstoricUncheckedCreateWithoutClientInput> | IstoricCreateWithoutClientInput[] | IstoricUncheckedCreateWithoutClientInput[]
    connectOrCreate?: IstoricCreateOrConnectWithoutClientInput | IstoricCreateOrConnectWithoutClientInput[]
    upsert?: IstoricUpsertWithWhereUniqueWithoutClientInput | IstoricUpsertWithWhereUniqueWithoutClientInput[]
    createMany?: IstoricCreateManyClientInputEnvelope
    set?: IstoricWhereUniqueInput | IstoricWhereUniqueInput[]
    disconnect?: IstoricWhereUniqueInput | IstoricWhereUniqueInput[]
    delete?: IstoricWhereUniqueInput | IstoricWhereUniqueInput[]
    connect?: IstoricWhereUniqueInput | IstoricWhereUniqueInput[]
    update?: IstoricUpdateWithWhereUniqueWithoutClientInput | IstoricUpdateWithWhereUniqueWithoutClientInput[]
    updateMany?: IstoricUpdateManyWithWhereWithoutClientInput | IstoricUpdateManyWithWhereWithoutClientInput[]
    deleteMany?: IstoricScalarWhereInput | IstoricScalarWhereInput[]
  }

  export type TaskUpdateManyWithoutClientNestedInput = {
    create?: XOR<TaskCreateWithoutClientInput, TaskUncheckedCreateWithoutClientInput> | TaskCreateWithoutClientInput[] | TaskUncheckedCreateWithoutClientInput[]
    connectOrCreate?: TaskCreateOrConnectWithoutClientInput | TaskCreateOrConnectWithoutClientInput[]
    upsert?: TaskUpsertWithWhereUniqueWithoutClientInput | TaskUpsertWithWhereUniqueWithoutClientInput[]
    createMany?: TaskCreateManyClientInputEnvelope
    set?: TaskWhereUniqueInput | TaskWhereUniqueInput[]
    disconnect?: TaskWhereUniqueInput | TaskWhereUniqueInput[]
    delete?: TaskWhereUniqueInput | TaskWhereUniqueInput[]
    connect?: TaskWhereUniqueInput | TaskWhereUniqueInput[]
    update?: TaskUpdateWithWhereUniqueWithoutClientInput | TaskUpdateWithWhereUniqueWithoutClientInput[]
    updateMany?: TaskUpdateManyWithWhereWithoutClientInput | TaskUpdateManyWithWhereWithoutClientInput[]
    deleteMany?: TaskScalarWhereInput | TaskScalarWhereInput[]
  }

  export type UserClientUpdateManyWithoutClientNestedInput = {
    create?: XOR<UserClientCreateWithoutClientInput, UserClientUncheckedCreateWithoutClientInput> | UserClientCreateWithoutClientInput[] | UserClientUncheckedCreateWithoutClientInput[]
    connectOrCreate?: UserClientCreateOrConnectWithoutClientInput | UserClientCreateOrConnectWithoutClientInput[]
    upsert?: UserClientUpsertWithWhereUniqueWithoutClientInput | UserClientUpsertWithWhereUniqueWithoutClientInput[]
    createMany?: UserClientCreateManyClientInputEnvelope
    set?: UserClientWhereUniqueInput | UserClientWhereUniqueInput[]
    disconnect?: UserClientWhereUniqueInput | UserClientWhereUniqueInput[]
    delete?: UserClientWhereUniqueInput | UserClientWhereUniqueInput[]
    connect?: UserClientWhereUniqueInput | UserClientWhereUniqueInput[]
    update?: UserClientUpdateWithWhereUniqueWithoutClientInput | UserClientUpdateWithWhereUniqueWithoutClientInput[]
    updateMany?: UserClientUpdateManyWithWhereWithoutClientInput | UserClientUpdateManyWithWhereWithoutClientInput[]
    deleteMany?: UserClientScalarWhereInput | UserClientScalarWhereInput[]
  }

  export type DetaliiUncheckedUpdateOneWithoutClientNestedInput = {
    create?: XOR<DetaliiCreateWithoutClientInput, DetaliiUncheckedCreateWithoutClientInput>
    connectOrCreate?: DetaliiCreateOrConnectWithoutClientInput
    upsert?: DetaliiUpsertWithoutClientInput
    disconnect?: DetaliiWhereInput | boolean
    delete?: DetaliiWhereInput | boolean
    connect?: DetaliiWhereUniqueInput
    update?: XOR<XOR<DetaliiUpdateToOneWithWhereWithoutClientInput, DetaliiUpdateWithoutClientInput>, DetaliiUncheckedUpdateWithoutClientInput>
  }

  export type PunctDeLucruUncheckedUpdateManyWithoutClientNestedInput = {
    create?: XOR<PunctDeLucruCreateWithoutClientInput, PunctDeLucruUncheckedCreateWithoutClientInput> | PunctDeLucruCreateWithoutClientInput[] | PunctDeLucruUncheckedCreateWithoutClientInput[]
    connectOrCreate?: PunctDeLucruCreateOrConnectWithoutClientInput | PunctDeLucruCreateOrConnectWithoutClientInput[]
    upsert?: PunctDeLucruUpsertWithWhereUniqueWithoutClientInput | PunctDeLucruUpsertWithWhereUniqueWithoutClientInput[]
    createMany?: PunctDeLucruCreateManyClientInputEnvelope
    set?: PunctDeLucruWhereUniqueInput | PunctDeLucruWhereUniqueInput[]
    disconnect?: PunctDeLucruWhereUniqueInput | PunctDeLucruWhereUniqueInput[]
    delete?: PunctDeLucruWhereUniqueInput | PunctDeLucruWhereUniqueInput[]
    connect?: PunctDeLucruWhereUniqueInput | PunctDeLucruWhereUniqueInput[]
    update?: PunctDeLucruUpdateWithWhereUniqueWithoutClientInput | PunctDeLucruUpdateWithWhereUniqueWithoutClientInput[]
    updateMany?: PunctDeLucruUpdateManyWithWhereWithoutClientInput | PunctDeLucruUpdateManyWithWhereWithoutClientInput[]
    deleteMany?: PunctDeLucruScalarWhereInput | PunctDeLucruScalarWhereInput[]
  }

  export type IstoricUncheckedUpdateManyWithoutClientNestedInput = {
    create?: XOR<IstoricCreateWithoutClientInput, IstoricUncheckedCreateWithoutClientInput> | IstoricCreateWithoutClientInput[] | IstoricUncheckedCreateWithoutClientInput[]
    connectOrCreate?: IstoricCreateOrConnectWithoutClientInput | IstoricCreateOrConnectWithoutClientInput[]
    upsert?: IstoricUpsertWithWhereUniqueWithoutClientInput | IstoricUpsertWithWhereUniqueWithoutClientInput[]
    createMany?: IstoricCreateManyClientInputEnvelope
    set?: IstoricWhereUniqueInput | IstoricWhereUniqueInput[]
    disconnect?: IstoricWhereUniqueInput | IstoricWhereUniqueInput[]
    delete?: IstoricWhereUniqueInput | IstoricWhereUniqueInput[]
    connect?: IstoricWhereUniqueInput | IstoricWhereUniqueInput[]
    update?: IstoricUpdateWithWhereUniqueWithoutClientInput | IstoricUpdateWithWhereUniqueWithoutClientInput[]
    updateMany?: IstoricUpdateManyWithWhereWithoutClientInput | IstoricUpdateManyWithWhereWithoutClientInput[]
    deleteMany?: IstoricScalarWhereInput | IstoricScalarWhereInput[]
  }

  export type TaskUncheckedUpdateManyWithoutClientNestedInput = {
    create?: XOR<TaskCreateWithoutClientInput, TaskUncheckedCreateWithoutClientInput> | TaskCreateWithoutClientInput[] | TaskUncheckedCreateWithoutClientInput[]
    connectOrCreate?: TaskCreateOrConnectWithoutClientInput | TaskCreateOrConnectWithoutClientInput[]
    upsert?: TaskUpsertWithWhereUniqueWithoutClientInput | TaskUpsertWithWhereUniqueWithoutClientInput[]
    createMany?: TaskCreateManyClientInputEnvelope
    set?: TaskWhereUniqueInput | TaskWhereUniqueInput[]
    disconnect?: TaskWhereUniqueInput | TaskWhereUniqueInput[]
    delete?: TaskWhereUniqueInput | TaskWhereUniqueInput[]
    connect?: TaskWhereUniqueInput | TaskWhereUniqueInput[]
    update?: TaskUpdateWithWhereUniqueWithoutClientInput | TaskUpdateWithWhereUniqueWithoutClientInput[]
    updateMany?: TaskUpdateManyWithWhereWithoutClientInput | TaskUpdateManyWithWhereWithoutClientInput[]
    deleteMany?: TaskScalarWhereInput | TaskScalarWhereInput[]
  }

  export type UserClientUncheckedUpdateManyWithoutClientNestedInput = {
    create?: XOR<UserClientCreateWithoutClientInput, UserClientUncheckedCreateWithoutClientInput> | UserClientCreateWithoutClientInput[] | UserClientUncheckedCreateWithoutClientInput[]
    connectOrCreate?: UserClientCreateOrConnectWithoutClientInput | UserClientCreateOrConnectWithoutClientInput[]
    upsert?: UserClientUpsertWithWhereUniqueWithoutClientInput | UserClientUpsertWithWhereUniqueWithoutClientInput[]
    createMany?: UserClientCreateManyClientInputEnvelope
    set?: UserClientWhereUniqueInput | UserClientWhereUniqueInput[]
    disconnect?: UserClientWhereUniqueInput | UserClientWhereUniqueInput[]
    delete?: UserClientWhereUniqueInput | UserClientWhereUniqueInput[]
    connect?: UserClientWhereUniqueInput | UserClientWhereUniqueInput[]
    update?: UserClientUpdateWithWhereUniqueWithoutClientInput | UserClientUpdateWithWhereUniqueWithoutClientInput[]
    updateMany?: UserClientUpdateManyWithWhereWithoutClientInput | UserClientUpdateManyWithWhereWithoutClientInput[]
    deleteMany?: UserClientScalarWhereInput | UserClientScalarWhereInput[]
  }

  export type ClientCreateNestedOneWithoutDetaliiInput = {
    create?: XOR<ClientCreateWithoutDetaliiInput, ClientUncheckedCreateWithoutDetaliiInput>
    connectOrCreate?: ClientCreateOrConnectWithoutDetaliiInput
    connect?: ClientWhereUniqueInput
  }

  export type EnumDaNuNuECazulFieldUpdateOperationsInput = {
    set?: $Enums.DaNuNuECazul
  }

  export type ClientUpdateOneRequiredWithoutDetaliiNestedInput = {
    create?: XOR<ClientCreateWithoutDetaliiInput, ClientUncheckedCreateWithoutDetaliiInput>
    connectOrCreate?: ClientCreateOrConnectWithoutDetaliiInput
    upsert?: ClientUpsertWithoutDetaliiInput
    connect?: ClientWhereUniqueInput
    update?: XOR<XOR<ClientUpdateToOneWithWhereWithoutDetaliiInput, ClientUpdateWithoutDetaliiInput>, ClientUncheckedUpdateWithoutDetaliiInput>
  }

  export type ClientCreateNestedOneWithoutPuncteDeLucruInput = {
    create?: XOR<ClientCreateWithoutPuncteDeLucruInput, ClientUncheckedCreateWithoutPuncteDeLucruInput>
    connectOrCreate?: ClientCreateOrConnectWithoutPuncteDeLucruInput
    connect?: ClientWhereUniqueInput
  }

  export type ClientUpdateOneRequiredWithoutPuncteDeLucruNestedInput = {
    create?: XOR<ClientCreateWithoutPuncteDeLucruInput, ClientUncheckedCreateWithoutPuncteDeLucruInput>
    connectOrCreate?: ClientCreateOrConnectWithoutPuncteDeLucruInput
    upsert?: ClientUpsertWithoutPuncteDeLucruInput
    connect?: ClientWhereUniqueInput
    update?: XOR<XOR<ClientUpdateToOneWithWhereWithoutPuncteDeLucruInput, ClientUpdateWithoutPuncteDeLucruInput>, ClientUncheckedUpdateWithoutPuncteDeLucruInput>
  }

  export type ClientCreateNestedOneWithoutIstoriceInput = {
    create?: XOR<ClientCreateWithoutIstoriceInput, ClientUncheckedCreateWithoutIstoriceInput>
    connectOrCreate?: ClientCreateOrConnectWithoutIstoriceInput
    connect?: ClientWhereUniqueInput
  }

  export type FloatFieldUpdateOperationsInput = {
    set?: number
    increment?: number
    decrement?: number
    multiply?: number
    divide?: number
  }

  export type ClientUpdateOneRequiredWithoutIstoriceNestedInput = {
    create?: XOR<ClientCreateWithoutIstoriceInput, ClientUncheckedCreateWithoutIstoriceInput>
    connectOrCreate?: ClientCreateOrConnectWithoutIstoriceInput
    upsert?: ClientUpsertWithoutIstoriceInput
    connect?: ClientWhereUniqueInput
    update?: XOR<XOR<ClientUpdateToOneWithWhereWithoutIstoriceInput, ClientUpdateWithoutIstoriceInput>, ClientUncheckedUpdateWithoutIstoriceInput>
  }

  export type UserCreateNestedOneWithoutTasksInput = {
    create?: XOR<UserCreateWithoutTasksInput, UserUncheckedCreateWithoutTasksInput>
    connectOrCreate?: UserCreateOrConnectWithoutTasksInput
    connect?: UserWhereUniqueInput
  }

  export type ClientCreateNestedOneWithoutTasksInput = {
    create?: XOR<ClientCreateWithoutTasksInput, ClientUncheckedCreateWithoutTasksInput>
    connectOrCreate?: ClientCreateOrConnectWithoutTasksInput
    connect?: ClientWhereUniqueInput
  }

  export type UserUpdateOneRequiredWithoutTasksNestedInput = {
    create?: XOR<UserCreateWithoutTasksInput, UserUncheckedCreateWithoutTasksInput>
    connectOrCreate?: UserCreateOrConnectWithoutTasksInput
    upsert?: UserUpsertWithoutTasksInput
    connect?: UserWhereUniqueInput
    update?: XOR<XOR<UserUpdateToOneWithWhereWithoutTasksInput, UserUpdateWithoutTasksInput>, UserUncheckedUpdateWithoutTasksInput>
  }

  export type ClientUpdateOneRequiredWithoutTasksNestedInput = {
    create?: XOR<ClientCreateWithoutTasksInput, ClientUncheckedCreateWithoutTasksInput>
    connectOrCreate?: ClientCreateOrConnectWithoutTasksInput
    upsert?: ClientUpsertWithoutTasksInput
    connect?: ClientWhereUniqueInput
    update?: XOR<XOR<ClientUpdateToOneWithWhereWithoutTasksInput, ClientUpdateWithoutTasksInput>, ClientUncheckedUpdateWithoutTasksInput>
  }

  export type RuleConditionCreateNestedManyWithoutRuleInput = {
    create?: XOR<RuleConditionCreateWithoutRuleInput, RuleConditionUncheckedCreateWithoutRuleInput> | RuleConditionCreateWithoutRuleInput[] | RuleConditionUncheckedCreateWithoutRuleInput[]
    connectOrCreate?: RuleConditionCreateOrConnectWithoutRuleInput | RuleConditionCreateOrConnectWithoutRuleInput[]
    createMany?: RuleConditionCreateManyRuleInputEnvelope
    connect?: RuleConditionWhereUniqueInput | RuleConditionWhereUniqueInput[]
  }

  export type RuleConditionUncheckedCreateNestedManyWithoutRuleInput = {
    create?: XOR<RuleConditionCreateWithoutRuleInput, RuleConditionUncheckedCreateWithoutRuleInput> | RuleConditionCreateWithoutRuleInput[] | RuleConditionUncheckedCreateWithoutRuleInput[]
    connectOrCreate?: RuleConditionCreateOrConnectWithoutRuleInput | RuleConditionCreateOrConnectWithoutRuleInput[]
    createMany?: RuleConditionCreateManyRuleInputEnvelope
    connect?: RuleConditionWhereUniqueInput | RuleConditionWhereUniqueInput[]
  }

  export type EnumFrequencyFieldUpdateOperationsInput = {
    set?: $Enums.Frequency
  }

  export type RuleConditionUpdateManyWithoutRuleNestedInput = {
    create?: XOR<RuleConditionCreateWithoutRuleInput, RuleConditionUncheckedCreateWithoutRuleInput> | RuleConditionCreateWithoutRuleInput[] | RuleConditionUncheckedCreateWithoutRuleInput[]
    connectOrCreate?: RuleConditionCreateOrConnectWithoutRuleInput | RuleConditionCreateOrConnectWithoutRuleInput[]
    upsert?: RuleConditionUpsertWithWhereUniqueWithoutRuleInput | RuleConditionUpsertWithWhereUniqueWithoutRuleInput[]
    createMany?: RuleConditionCreateManyRuleInputEnvelope
    set?: RuleConditionWhereUniqueInput | RuleConditionWhereUniqueInput[]
    disconnect?: RuleConditionWhereUniqueInput | RuleConditionWhereUniqueInput[]
    delete?: RuleConditionWhereUniqueInput | RuleConditionWhereUniqueInput[]
    connect?: RuleConditionWhereUniqueInput | RuleConditionWhereUniqueInput[]
    update?: RuleConditionUpdateWithWhereUniqueWithoutRuleInput | RuleConditionUpdateWithWhereUniqueWithoutRuleInput[]
    updateMany?: RuleConditionUpdateManyWithWhereWithoutRuleInput | RuleConditionUpdateManyWithWhereWithoutRuleInput[]
    deleteMany?: RuleConditionScalarWhereInput | RuleConditionScalarWhereInput[]
  }

  export type RuleConditionUncheckedUpdateManyWithoutRuleNestedInput = {
    create?: XOR<RuleConditionCreateWithoutRuleInput, RuleConditionUncheckedCreateWithoutRuleInput> | RuleConditionCreateWithoutRuleInput[] | RuleConditionUncheckedCreateWithoutRuleInput[]
    connectOrCreate?: RuleConditionCreateOrConnectWithoutRuleInput | RuleConditionCreateOrConnectWithoutRuleInput[]
    upsert?: RuleConditionUpsertWithWhereUniqueWithoutRuleInput | RuleConditionUpsertWithWhereUniqueWithoutRuleInput[]
    createMany?: RuleConditionCreateManyRuleInputEnvelope
    set?: RuleConditionWhereUniqueInput | RuleConditionWhereUniqueInput[]
    disconnect?: RuleConditionWhereUniqueInput | RuleConditionWhereUniqueInput[]
    delete?: RuleConditionWhereUniqueInput | RuleConditionWhereUniqueInput[]
    connect?: RuleConditionWhereUniqueInput | RuleConditionWhereUniqueInput[]
    update?: RuleConditionUpdateWithWhereUniqueWithoutRuleInput | RuleConditionUpdateWithWhereUniqueWithoutRuleInput[]
    updateMany?: RuleConditionUpdateManyWithWhereWithoutRuleInput | RuleConditionUpdateManyWithWhereWithoutRuleInput[]
    deleteMany?: RuleConditionScalarWhereInput | RuleConditionScalarWhereInput[]
  }

  export type RuleCreateNestedOneWithoutConditionsInput = {
    create?: XOR<RuleCreateWithoutConditionsInput, RuleUncheckedCreateWithoutConditionsInput>
    connectOrCreate?: RuleCreateOrConnectWithoutConditionsInput
    connect?: RuleWhereUniqueInput
  }

  export type EnumConditionOperatorFieldUpdateOperationsInput = {
    set?: $Enums.ConditionOperator
  }

  export type RuleUpdateOneRequiredWithoutConditionsNestedInput = {
    create?: XOR<RuleCreateWithoutConditionsInput, RuleUncheckedCreateWithoutConditionsInput>
    connectOrCreate?: RuleCreateOrConnectWithoutConditionsInput
    upsert?: RuleUpsertWithoutConditionsInput
    connect?: RuleWhereUniqueInput
    update?: XOR<XOR<RuleUpdateToOneWithWhereWithoutConditionsInput, RuleUpdateWithoutConditionsInput>, RuleUncheckedUpdateWithoutConditionsInput>
  }

  export type UserCreateNestedOneWithoutClientsInput = {
    create?: XOR<UserCreateWithoutClientsInput, UserUncheckedCreateWithoutClientsInput>
    connectOrCreate?: UserCreateOrConnectWithoutClientsInput
    connect?: UserWhereUniqueInput
  }

  export type ClientCreateNestedOneWithoutUsersInput = {
    create?: XOR<ClientCreateWithoutUsersInput, ClientUncheckedCreateWithoutUsersInput>
    connectOrCreate?: ClientCreateOrConnectWithoutUsersInput
    connect?: ClientWhereUniqueInput
  }

  export type UserUpdateOneRequiredWithoutClientsNestedInput = {
    create?: XOR<UserCreateWithoutClientsInput, UserUncheckedCreateWithoutClientsInput>
    connectOrCreate?: UserCreateOrConnectWithoutClientsInput
    upsert?: UserUpsertWithoutClientsInput
    connect?: UserWhereUniqueInput
    update?: XOR<XOR<UserUpdateToOneWithWhereWithoutClientsInput, UserUpdateWithoutClientsInput>, UserUncheckedUpdateWithoutClientsInput>
  }

  export type ClientUpdateOneRequiredWithoutUsersNestedInput = {
    create?: XOR<ClientCreateWithoutUsersInput, ClientUncheckedCreateWithoutUsersInput>
    connectOrCreate?: ClientCreateOrConnectWithoutUsersInput
    upsert?: ClientUpsertWithoutUsersInput
    connect?: ClientWhereUniqueInput
    update?: XOR<XOR<ClientUpdateToOneWithWhereWithoutUsersInput, ClientUpdateWithoutUsersInput>, ClientUncheckedUpdateWithoutUsersInput>
  }

  export type NestedIntFilter<$PrismaModel = never> = {
    equals?: number | IntFieldRefInput<$PrismaModel>
    in?: number[] | ListIntFieldRefInput<$PrismaModel>
    notIn?: number[] | ListIntFieldRefInput<$PrismaModel>
    lt?: number | IntFieldRefInput<$PrismaModel>
    lte?: number | IntFieldRefInput<$PrismaModel>
    gt?: number | IntFieldRefInput<$PrismaModel>
    gte?: number | IntFieldRefInput<$PrismaModel>
    not?: NestedIntFilter<$PrismaModel> | number
  }

  export type NestedStringFilter<$PrismaModel = never> = {
    equals?: string | StringFieldRefInput<$PrismaModel>
    in?: string[] | ListStringFieldRefInput<$PrismaModel>
    notIn?: string[] | ListStringFieldRefInput<$PrismaModel>
    lt?: string | StringFieldRefInput<$PrismaModel>
    lte?: string | StringFieldRefInput<$PrismaModel>
    gt?: string | StringFieldRefInput<$PrismaModel>
    gte?: string | StringFieldRefInput<$PrismaModel>
    contains?: string | StringFieldRefInput<$PrismaModel>
    startsWith?: string | StringFieldRefInput<$PrismaModel>
    endsWith?: string | StringFieldRefInput<$PrismaModel>
    not?: NestedStringFilter<$PrismaModel> | string
  }

  export type NestedStringNullableFilter<$PrismaModel = never> = {
    equals?: string | StringFieldRefInput<$PrismaModel> | null
    in?: string[] | ListStringFieldRefInput<$PrismaModel> | null
    notIn?: string[] | ListStringFieldRefInput<$PrismaModel> | null
    lt?: string | StringFieldRefInput<$PrismaModel>
    lte?: string | StringFieldRefInput<$PrismaModel>
    gt?: string | StringFieldRefInput<$PrismaModel>
    gte?: string | StringFieldRefInput<$PrismaModel>
    contains?: string | StringFieldRefInput<$PrismaModel>
    startsWith?: string | StringFieldRefInput<$PrismaModel>
    endsWith?: string | StringFieldRefInput<$PrismaModel>
    not?: NestedStringNullableFilter<$PrismaModel> | string | null
  }

  export type NestedEnumRoleFilter<$PrismaModel = never> = {
    equals?: $Enums.Role | EnumRoleFieldRefInput<$PrismaModel>
    in?: $Enums.Role[] | ListEnumRoleFieldRefInput<$PrismaModel>
    notIn?: $Enums.Role[] | ListEnumRoleFieldRefInput<$PrismaModel>
    not?: NestedEnumRoleFilter<$PrismaModel> | $Enums.Role
  }

  export type NestedIntWithAggregatesFilter<$PrismaModel = never> = {
    equals?: number | IntFieldRefInput<$PrismaModel>
    in?: number[] | ListIntFieldRefInput<$PrismaModel>
    notIn?: number[] | ListIntFieldRefInput<$PrismaModel>
    lt?: number | IntFieldRefInput<$PrismaModel>
    lte?: number | IntFieldRefInput<$PrismaModel>
    gt?: number | IntFieldRefInput<$PrismaModel>
    gte?: number | IntFieldRefInput<$PrismaModel>
    not?: NestedIntWithAggregatesFilter<$PrismaModel> | number
    _count?: NestedIntFilter<$PrismaModel>
    _avg?: NestedFloatFilter<$PrismaModel>
    _sum?: NestedIntFilter<$PrismaModel>
    _min?: NestedIntFilter<$PrismaModel>
    _max?: NestedIntFilter<$PrismaModel>
  }

  export type NestedFloatFilter<$PrismaModel = never> = {
    equals?: number | FloatFieldRefInput<$PrismaModel>
    in?: number[] | ListFloatFieldRefInput<$PrismaModel>
    notIn?: number[] | ListFloatFieldRefInput<$PrismaModel>
    lt?: number | FloatFieldRefInput<$PrismaModel>
    lte?: number | FloatFieldRefInput<$PrismaModel>
    gt?: number | FloatFieldRefInput<$PrismaModel>
    gte?: number | FloatFieldRefInput<$PrismaModel>
    not?: NestedFloatFilter<$PrismaModel> | number
  }

  export type NestedStringWithAggregatesFilter<$PrismaModel = never> = {
    equals?: string | StringFieldRefInput<$PrismaModel>
    in?: string[] | ListStringFieldRefInput<$PrismaModel>
    notIn?: string[] | ListStringFieldRefInput<$PrismaModel>
    lt?: string | StringFieldRefInput<$PrismaModel>
    lte?: string | StringFieldRefInput<$PrismaModel>
    gt?: string | StringFieldRefInput<$PrismaModel>
    gte?: string | StringFieldRefInput<$PrismaModel>
    contains?: string | StringFieldRefInput<$PrismaModel>
    startsWith?: string | StringFieldRefInput<$PrismaModel>
    endsWith?: string | StringFieldRefInput<$PrismaModel>
    not?: NestedStringWithAggregatesFilter<$PrismaModel> | string
    _count?: NestedIntFilter<$PrismaModel>
    _min?: NestedStringFilter<$PrismaModel>
    _max?: NestedStringFilter<$PrismaModel>
  }

  export type NestedStringNullableWithAggregatesFilter<$PrismaModel = never> = {
    equals?: string | StringFieldRefInput<$PrismaModel> | null
    in?: string[] | ListStringFieldRefInput<$PrismaModel> | null
    notIn?: string[] | ListStringFieldRefInput<$PrismaModel> | null
    lt?: string | StringFieldRefInput<$PrismaModel>
    lte?: string | StringFieldRefInput<$PrismaModel>
    gt?: string | StringFieldRefInput<$PrismaModel>
    gte?: string | StringFieldRefInput<$PrismaModel>
    contains?: string | StringFieldRefInput<$PrismaModel>
    startsWith?: string | StringFieldRefInput<$PrismaModel>
    endsWith?: string | StringFieldRefInput<$PrismaModel>
    not?: NestedStringNullableWithAggregatesFilter<$PrismaModel> | string | null
    _count?: NestedIntNullableFilter<$PrismaModel>
    _min?: NestedStringNullableFilter<$PrismaModel>
    _max?: NestedStringNullableFilter<$PrismaModel>
  }

  export type NestedIntNullableFilter<$PrismaModel = never> = {
    equals?: number | IntFieldRefInput<$PrismaModel> | null
    in?: number[] | ListIntFieldRefInput<$PrismaModel> | null
    notIn?: number[] | ListIntFieldRefInput<$PrismaModel> | null
    lt?: number | IntFieldRefInput<$PrismaModel>
    lte?: number | IntFieldRefInput<$PrismaModel>
    gt?: number | IntFieldRefInput<$PrismaModel>
    gte?: number | IntFieldRefInput<$PrismaModel>
    not?: NestedIntNullableFilter<$PrismaModel> | number | null
  }

  export type NestedEnumRoleWithAggregatesFilter<$PrismaModel = never> = {
    equals?: $Enums.Role | EnumRoleFieldRefInput<$PrismaModel>
    in?: $Enums.Role[] | ListEnumRoleFieldRefInput<$PrismaModel>
    notIn?: $Enums.Role[] | ListEnumRoleFieldRefInput<$PrismaModel>
    not?: NestedEnumRoleWithAggregatesFilter<$PrismaModel> | $Enums.Role
    _count?: NestedIntFilter<$PrismaModel>
    _min?: NestedEnumRoleFilter<$PrismaModel>
    _max?: NestedEnumRoleFilter<$PrismaModel>
  }

  export type NestedEnumTipFilter<$PrismaModel = never> = {
    equals?: $Enums.Tip | EnumTipFieldRefInput<$PrismaModel>
    in?: $Enums.Tip[] | ListEnumTipFieldRefInput<$PrismaModel>
    notIn?: $Enums.Tip[] | ListEnumTipFieldRefInput<$PrismaModel>
    not?: NestedEnumTipFilter<$PrismaModel> | $Enums.Tip
  }

  export type NestedBoolFilter<$PrismaModel = never> = {
    equals?: boolean | BooleanFieldRefInput<$PrismaModel>
    not?: NestedBoolFilter<$PrismaModel> | boolean
  }

  export type NestedDateTimeNullableFilter<$PrismaModel = never> = {
    equals?: Date | string | DateTimeFieldRefInput<$PrismaModel> | null
    in?: Date[] | string[] | ListDateTimeFieldRefInput<$PrismaModel> | null
    notIn?: Date[] | string[] | ListDateTimeFieldRefInput<$PrismaModel> | null
    lt?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    lte?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    gt?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    gte?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    not?: NestedDateTimeNullableFilter<$PrismaModel> | Date | string | null
  }

  export type NestedEnumAdministratieFilter<$PrismaModel = never> = {
    equals?: $Enums.Administratie | EnumAdministratieFieldRefInput<$PrismaModel>
    in?: $Enums.Administratie[] | ListEnumAdministratieFieldRefInput<$PrismaModel>
    notIn?: $Enums.Administratie[] | ListEnumAdministratieFieldRefInput<$PrismaModel>
    not?: NestedEnumAdministratieFilter<$PrismaModel> | $Enums.Administratie
  }

  export type NestedEnumImpozitNullableFilter<$PrismaModel = never> = {
    equals?: $Enums.Impozit | EnumImpozitFieldRefInput<$PrismaModel> | null
    in?: $Enums.Impozit[] | ListEnumImpozitFieldRefInput<$PrismaModel> | null
    notIn?: $Enums.Impozit[] | ListEnumImpozitFieldRefInput<$PrismaModel> | null
    not?: NestedEnumImpozitNullableFilter<$PrismaModel> | $Enums.Impozit | null
  }

  export type NestedEnumDaLunarTrimFilter<$PrismaModel = never> = {
    equals?: $Enums.DaLunarTrim | EnumDaLunarTrimFieldRefInput<$PrismaModel>
    in?: $Enums.DaLunarTrim[] | ListEnumDaLunarTrimFieldRefInput<$PrismaModel>
    notIn?: $Enums.DaLunarTrim[] | ListEnumDaLunarTrimFieldRefInput<$PrismaModel>
    not?: NestedEnumDaLunarTrimFilter<$PrismaModel> | $Enums.DaLunarTrim
  }

  export type NestedBoolNullableFilter<$PrismaModel = never> = {
    equals?: boolean | BooleanFieldRefInput<$PrismaModel> | null
    not?: NestedBoolNullableFilter<$PrismaModel> | boolean | null
  }

  export type NestedEnumDaLunarTrimNullableFilter<$PrismaModel = never> = {
    equals?: $Enums.DaLunarTrim | EnumDaLunarTrimFieldRefInput<$PrismaModel> | null
    in?: $Enums.DaLunarTrim[] | ListEnumDaLunarTrimFieldRefInput<$PrismaModel> | null
    notIn?: $Enums.DaLunarTrim[] | ListEnumDaLunarTrimFieldRefInput<$PrismaModel> | null
    not?: NestedEnumDaLunarTrimNullableFilter<$PrismaModel> | $Enums.DaLunarTrim | null
  }

  export type NestedDateTimeFilter<$PrismaModel = never> = {
    equals?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    in?: Date[] | string[] | ListDateTimeFieldRefInput<$PrismaModel>
    notIn?: Date[] | string[] | ListDateTimeFieldRefInput<$PrismaModel>
    lt?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    lte?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    gt?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    gte?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    not?: NestedDateTimeFilter<$PrismaModel> | Date | string
  }

  export type NestedEnumTipWithAggregatesFilter<$PrismaModel = never> = {
    equals?: $Enums.Tip | EnumTipFieldRefInput<$PrismaModel>
    in?: $Enums.Tip[] | ListEnumTipFieldRefInput<$PrismaModel>
    notIn?: $Enums.Tip[] | ListEnumTipFieldRefInput<$PrismaModel>
    not?: NestedEnumTipWithAggregatesFilter<$PrismaModel> | $Enums.Tip
    _count?: NestedIntFilter<$PrismaModel>
    _min?: NestedEnumTipFilter<$PrismaModel>
    _max?: NestedEnumTipFilter<$PrismaModel>
  }

  export type NestedBoolWithAggregatesFilter<$PrismaModel = never> = {
    equals?: boolean | BooleanFieldRefInput<$PrismaModel>
    not?: NestedBoolWithAggregatesFilter<$PrismaModel> | boolean
    _count?: NestedIntFilter<$PrismaModel>
    _min?: NestedBoolFilter<$PrismaModel>
    _max?: NestedBoolFilter<$PrismaModel>
  }

  export type NestedDateTimeNullableWithAggregatesFilter<$PrismaModel = never> = {
    equals?: Date | string | DateTimeFieldRefInput<$PrismaModel> | null
    in?: Date[] | string[] | ListDateTimeFieldRefInput<$PrismaModel> | null
    notIn?: Date[] | string[] | ListDateTimeFieldRefInput<$PrismaModel> | null
    lt?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    lte?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    gt?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    gte?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    not?: NestedDateTimeNullableWithAggregatesFilter<$PrismaModel> | Date | string | null
    _count?: NestedIntNullableFilter<$PrismaModel>
    _min?: NestedDateTimeNullableFilter<$PrismaModel>
    _max?: NestedDateTimeNullableFilter<$PrismaModel>
  }

  export type NestedEnumAdministratieWithAggregatesFilter<$PrismaModel = never> = {
    equals?: $Enums.Administratie | EnumAdministratieFieldRefInput<$PrismaModel>
    in?: $Enums.Administratie[] | ListEnumAdministratieFieldRefInput<$PrismaModel>
    notIn?: $Enums.Administratie[] | ListEnumAdministratieFieldRefInput<$PrismaModel>
    not?: NestedEnumAdministratieWithAggregatesFilter<$PrismaModel> | $Enums.Administratie
    _count?: NestedIntFilter<$PrismaModel>
    _min?: NestedEnumAdministratieFilter<$PrismaModel>
    _max?: NestedEnumAdministratieFilter<$PrismaModel>
  }

  export type NestedEnumImpozitNullableWithAggregatesFilter<$PrismaModel = never> = {
    equals?: $Enums.Impozit | EnumImpozitFieldRefInput<$PrismaModel> | null
    in?: $Enums.Impozit[] | ListEnumImpozitFieldRefInput<$PrismaModel> | null
    notIn?: $Enums.Impozit[] | ListEnumImpozitFieldRefInput<$PrismaModel> | null
    not?: NestedEnumImpozitNullableWithAggregatesFilter<$PrismaModel> | $Enums.Impozit | null
    _count?: NestedIntNullableFilter<$PrismaModel>
    _min?: NestedEnumImpozitNullableFilter<$PrismaModel>
    _max?: NestedEnumImpozitNullableFilter<$PrismaModel>
  }

  export type NestedEnumDaLunarTrimWithAggregatesFilter<$PrismaModel = never> = {
    equals?: $Enums.DaLunarTrim | EnumDaLunarTrimFieldRefInput<$PrismaModel>
    in?: $Enums.DaLunarTrim[] | ListEnumDaLunarTrimFieldRefInput<$PrismaModel>
    notIn?: $Enums.DaLunarTrim[] | ListEnumDaLunarTrimFieldRefInput<$PrismaModel>
    not?: NestedEnumDaLunarTrimWithAggregatesFilter<$PrismaModel> | $Enums.DaLunarTrim
    _count?: NestedIntFilter<$PrismaModel>
    _min?: NestedEnumDaLunarTrimFilter<$PrismaModel>
    _max?: NestedEnumDaLunarTrimFilter<$PrismaModel>
  }

  export type NestedBoolNullableWithAggregatesFilter<$PrismaModel = never> = {
    equals?: boolean | BooleanFieldRefInput<$PrismaModel> | null
    not?: NestedBoolNullableWithAggregatesFilter<$PrismaModel> | boolean | null
    _count?: NestedIntNullableFilter<$PrismaModel>
    _min?: NestedBoolNullableFilter<$PrismaModel>
    _max?: NestedBoolNullableFilter<$PrismaModel>
  }

  export type NestedEnumDaLunarTrimNullableWithAggregatesFilter<$PrismaModel = never> = {
    equals?: $Enums.DaLunarTrim | EnumDaLunarTrimFieldRefInput<$PrismaModel> | null
    in?: $Enums.DaLunarTrim[] | ListEnumDaLunarTrimFieldRefInput<$PrismaModel> | null
    notIn?: $Enums.DaLunarTrim[] | ListEnumDaLunarTrimFieldRefInput<$PrismaModel> | null
    not?: NestedEnumDaLunarTrimNullableWithAggregatesFilter<$PrismaModel> | $Enums.DaLunarTrim | null
    _count?: NestedIntNullableFilter<$PrismaModel>
    _min?: NestedEnumDaLunarTrimNullableFilter<$PrismaModel>
    _max?: NestedEnumDaLunarTrimNullableFilter<$PrismaModel>
  }

  export type NestedDateTimeWithAggregatesFilter<$PrismaModel = never> = {
    equals?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    in?: Date[] | string[] | ListDateTimeFieldRefInput<$PrismaModel>
    notIn?: Date[] | string[] | ListDateTimeFieldRefInput<$PrismaModel>
    lt?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    lte?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    gt?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    gte?: Date | string | DateTimeFieldRefInput<$PrismaModel>
    not?: NestedDateTimeWithAggregatesFilter<$PrismaModel> | Date | string
    _count?: NestedIntFilter<$PrismaModel>
    _min?: NestedDateTimeFilter<$PrismaModel>
    _max?: NestedDateTimeFilter<$PrismaModel>
  }

  export type NestedEnumDaNuNuECazulFilter<$PrismaModel = never> = {
    equals?: $Enums.DaNuNuECazul | EnumDaNuNuECazulFieldRefInput<$PrismaModel>
    in?: $Enums.DaNuNuECazul[] | ListEnumDaNuNuECazulFieldRefInput<$PrismaModel>
    notIn?: $Enums.DaNuNuECazul[] | ListEnumDaNuNuECazulFieldRefInput<$PrismaModel>
    not?: NestedEnumDaNuNuECazulFilter<$PrismaModel> | $Enums.DaNuNuECazul
  }

  export type NestedEnumDaNuNuECazulWithAggregatesFilter<$PrismaModel = never> = {
    equals?: $Enums.DaNuNuECazul | EnumDaNuNuECazulFieldRefInput<$PrismaModel>
    in?: $Enums.DaNuNuECazul[] | ListEnumDaNuNuECazulFieldRefInput<$PrismaModel>
    notIn?: $Enums.DaNuNuECazul[] | ListEnumDaNuNuECazulFieldRefInput<$PrismaModel>
    not?: NestedEnumDaNuNuECazulWithAggregatesFilter<$PrismaModel> | $Enums.DaNuNuECazul
    _count?: NestedIntFilter<$PrismaModel>
    _min?: NestedEnumDaNuNuECazulFilter<$PrismaModel>
    _max?: NestedEnumDaNuNuECazulFilter<$PrismaModel>
  }

  export type NestedFloatWithAggregatesFilter<$PrismaModel = never> = {
    equals?: number | FloatFieldRefInput<$PrismaModel>
    in?: number[] | ListFloatFieldRefInput<$PrismaModel>
    notIn?: number[] | ListFloatFieldRefInput<$PrismaModel>
    lt?: number | FloatFieldRefInput<$PrismaModel>
    lte?: number | FloatFieldRefInput<$PrismaModel>
    gt?: number | FloatFieldRefInput<$PrismaModel>
    gte?: number | FloatFieldRefInput<$PrismaModel>
    not?: NestedFloatWithAggregatesFilter<$PrismaModel> | number
    _count?: NestedIntFilter<$PrismaModel>
    _avg?: NestedFloatFilter<$PrismaModel>
    _sum?: NestedFloatFilter<$PrismaModel>
    _min?: NestedFloatFilter<$PrismaModel>
    _max?: NestedFloatFilter<$PrismaModel>
  }

  export type NestedEnumFrequencyFilter<$PrismaModel = never> = {
    equals?: $Enums.Frequency | EnumFrequencyFieldRefInput<$PrismaModel>
    in?: $Enums.Frequency[] | ListEnumFrequencyFieldRefInput<$PrismaModel>
    notIn?: $Enums.Frequency[] | ListEnumFrequencyFieldRefInput<$PrismaModel>
    not?: NestedEnumFrequencyFilter<$PrismaModel> | $Enums.Frequency
  }

  export type NestedEnumFrequencyWithAggregatesFilter<$PrismaModel = never> = {
    equals?: $Enums.Frequency | EnumFrequencyFieldRefInput<$PrismaModel>
    in?: $Enums.Frequency[] | ListEnumFrequencyFieldRefInput<$PrismaModel>
    notIn?: $Enums.Frequency[] | ListEnumFrequencyFieldRefInput<$PrismaModel>
    not?: NestedEnumFrequencyWithAggregatesFilter<$PrismaModel> | $Enums.Frequency
    _count?: NestedIntFilter<$PrismaModel>
    _min?: NestedEnumFrequencyFilter<$PrismaModel>
    _max?: NestedEnumFrequencyFilter<$PrismaModel>
  }

  export type NestedEnumConditionOperatorFilter<$PrismaModel = never> = {
    equals?: $Enums.ConditionOperator | EnumConditionOperatorFieldRefInput<$PrismaModel>
    in?: $Enums.ConditionOperator[] | ListEnumConditionOperatorFieldRefInput<$PrismaModel>
    notIn?: $Enums.ConditionOperator[] | ListEnumConditionOperatorFieldRefInput<$PrismaModel>
    not?: NestedEnumConditionOperatorFilter<$PrismaModel> | $Enums.ConditionOperator
  }

  export type NestedEnumConditionOperatorWithAggregatesFilter<$PrismaModel = never> = {
    equals?: $Enums.ConditionOperator | EnumConditionOperatorFieldRefInput<$PrismaModel>
    in?: $Enums.ConditionOperator[] | ListEnumConditionOperatorFieldRefInput<$PrismaModel>
    notIn?: $Enums.ConditionOperator[] | ListEnumConditionOperatorFieldRefInput<$PrismaModel>
    not?: NestedEnumConditionOperatorWithAggregatesFilter<$PrismaModel> | $Enums.ConditionOperator
    _count?: NestedIntFilter<$PrismaModel>
    _min?: NestedEnumConditionOperatorFilter<$PrismaModel>
    _max?: NestedEnumConditionOperatorFilter<$PrismaModel>
  }

  export type TaskCreateWithoutUserInput = {
    done?: boolean
    title: string
    notes?: string | null
    blocked?: string | null
    objective?: string | null
    date: Date | string
    client: ClientCreateNestedOneWithoutTasksInput
  }

  export type TaskUncheckedCreateWithoutUserInput = {
    id?: number
    done?: boolean
    title: string
    notes?: string | null
    blocked?: string | null
    objective?: string | null
    date: Date | string
    clientId: number
  }

  export type TaskCreateOrConnectWithoutUserInput = {
    where: TaskWhereUniqueInput
    create: XOR<TaskCreateWithoutUserInput, TaskUncheckedCreateWithoutUserInput>
  }

  export type TaskCreateManyUserInputEnvelope = {
    data: TaskCreateManyUserInput | TaskCreateManyUserInput[]
    skipDuplicates?: boolean
  }

  export type UserClientCreateWithoutUserInput = {
    client: ClientCreateNestedOneWithoutUsersInput
  }

  export type UserClientUncheckedCreateWithoutUserInput = {
    id?: number
    clientId: number
  }

  export type UserClientCreateOrConnectWithoutUserInput = {
    where: UserClientWhereUniqueInput
    create: XOR<UserClientCreateWithoutUserInput, UserClientUncheckedCreateWithoutUserInput>
  }

  export type UserClientCreateManyUserInputEnvelope = {
    data: UserClientCreateManyUserInput | UserClientCreateManyUserInput[]
    skipDuplicates?: boolean
  }

  export type TaskUpsertWithWhereUniqueWithoutUserInput = {
    where: TaskWhereUniqueInput
    update: XOR<TaskUpdateWithoutUserInput, TaskUncheckedUpdateWithoutUserInput>
    create: XOR<TaskCreateWithoutUserInput, TaskUncheckedCreateWithoutUserInput>
  }

  export type TaskUpdateWithWhereUniqueWithoutUserInput = {
    where: TaskWhereUniqueInput
    data: XOR<TaskUpdateWithoutUserInput, TaskUncheckedUpdateWithoutUserInput>
  }

  export type TaskUpdateManyWithWhereWithoutUserInput = {
    where: TaskScalarWhereInput
    data: XOR<TaskUpdateManyMutationInput, TaskUncheckedUpdateManyWithoutUserInput>
  }

  export type TaskScalarWhereInput = {
    AND?: TaskScalarWhereInput | TaskScalarWhereInput[]
    OR?: TaskScalarWhereInput[]
    NOT?: TaskScalarWhereInput | TaskScalarWhereInput[]
    id?: IntFilter<"Task"> | number
    done?: BoolFilter<"Task"> | boolean
    title?: StringFilter<"Task"> | string
    notes?: StringNullableFilter<"Task"> | string | null
    blocked?: StringNullableFilter<"Task"> | string | null
    objective?: StringNullableFilter<"Task"> | string | null
    date?: DateTimeFilter<"Task"> | Date | string
    userId?: IntFilter<"Task"> | number
    clientId?: IntFilter<"Task"> | number
  }

  export type UserClientUpsertWithWhereUniqueWithoutUserInput = {
    where: UserClientWhereUniqueInput
    update: XOR<UserClientUpdateWithoutUserInput, UserClientUncheckedUpdateWithoutUserInput>
    create: XOR<UserClientCreateWithoutUserInput, UserClientUncheckedCreateWithoutUserInput>
  }

  export type UserClientUpdateWithWhereUniqueWithoutUserInput = {
    where: UserClientWhereUniqueInput
    data: XOR<UserClientUpdateWithoutUserInput, UserClientUncheckedUpdateWithoutUserInput>
  }

  export type UserClientUpdateManyWithWhereWithoutUserInput = {
    where: UserClientScalarWhereInput
    data: XOR<UserClientUpdateManyMutationInput, UserClientUncheckedUpdateManyWithoutUserInput>
  }

  export type UserClientScalarWhereInput = {
    AND?: UserClientScalarWhereInput | UserClientScalarWhereInput[]
    OR?: UserClientScalarWhereInput[]
    NOT?: UserClientScalarWhereInput | UserClientScalarWhereInput[]
    id?: IntFilter<"UserClient"> | number
    userId?: IntFilter<"UserClient"> | number
    clientId?: IntFilter<"UserClient"> | number
  }

  export type DetaliiCreateWithoutClientInput = {
    registruUC: boolean
    registruEvFiscala: $Enums.DaNuNuECazul
    ofSpalareBani: boolean
    regulamentOrdineInterioara: boolean
    manualPoliticiContabile: boolean
    adresaRevisal: boolean
    parolaITM?: string | null
    depunereDeclaratiiOnline: boolean
    accesDosarFiscal: $Enums.DaNuNuECazul
  }

  export type DetaliiUncheckedCreateWithoutClientInput = {
    id?: number
    registruUC: boolean
    registruEvFiscala: $Enums.DaNuNuECazul
    ofSpalareBani: boolean
    regulamentOrdineInterioara: boolean
    manualPoliticiContabile: boolean
    adresaRevisal: boolean
    parolaITM?: string | null
    depunereDeclaratiiOnline: boolean
    accesDosarFiscal: $Enums.DaNuNuECazul
  }

  export type DetaliiCreateOrConnectWithoutClientInput = {
    where: DetaliiWhereUniqueInput
    create: XOR<DetaliiCreateWithoutClientInput, DetaliiUncheckedCreateWithoutClientInput>
  }

  export type PunctDeLucruCreateWithoutClientInput = {
    denumire: string
    deLa: Date | string
    panaLa?: Date | string | null
    administratie: $Enums.Administratie
    registruUC: boolean
    salariati: number
    cui?: string | null
    casaDeMarcat: boolean
  }

  export type PunctDeLucruUncheckedCreateWithoutClientInput = {
    id?: number
    denumire: string
    deLa: Date | string
    panaLa?: Date | string | null
    administratie: $Enums.Administratie
    registruUC: boolean
    salariati: number
    cui?: string | null
    casaDeMarcat: boolean
  }

  export type PunctDeLucruCreateOrConnectWithoutClientInput = {
    where: PunctDeLucruWhereUniqueInput
    create: XOR<PunctDeLucruCreateWithoutClientInput, PunctDeLucruUncheckedCreateWithoutClientInput>
  }

  export type PunctDeLucruCreateManyClientInputEnvelope = {
    data: PunctDeLucruCreateManyClientInput | PunctDeLucruCreateManyClientInput[]
    skipDuplicates?: boolean
  }

  export type IstoricCreateWithoutClientInput = {
    anul: number
    cifraAfaceri: number
    inventar: boolean
    bilantSemIun: $Enums.DaNuNuECazul
    bilantAnual: $Enums.DaNuNuECazul
  }

  export type IstoricUncheckedCreateWithoutClientInput = {
    id?: number
    anul: number
    cifraAfaceri: number
    inventar: boolean
    bilantSemIun: $Enums.DaNuNuECazul
    bilantAnual: $Enums.DaNuNuECazul
  }

  export type IstoricCreateOrConnectWithoutClientInput = {
    where: IstoricWhereUniqueInput
    create: XOR<IstoricCreateWithoutClientInput, IstoricUncheckedCreateWithoutClientInput>
  }

  export type IstoricCreateManyClientInputEnvelope = {
    data: IstoricCreateManyClientInput | IstoricCreateManyClientInput[]
    skipDuplicates?: boolean
  }

  export type TaskCreateWithoutClientInput = {
    done?: boolean
    title: string
    notes?: string | null
    blocked?: string | null
    objective?: string | null
    date: Date | string
    user: UserCreateNestedOneWithoutTasksInput
  }

  export type TaskUncheckedCreateWithoutClientInput = {
    id?: number
    done?: boolean
    title: string
    notes?: string | null
    blocked?: string | null
    objective?: string | null
    date: Date | string
    userId: number
  }

  export type TaskCreateOrConnectWithoutClientInput = {
    where: TaskWhereUniqueInput
    create: XOR<TaskCreateWithoutClientInput, TaskUncheckedCreateWithoutClientInput>
  }

  export type TaskCreateManyClientInputEnvelope = {
    data: TaskCreateManyClientInput | TaskCreateManyClientInput[]
    skipDuplicates?: boolean
  }

  export type UserClientCreateWithoutClientInput = {
    user: UserCreateNestedOneWithoutClientsInput
  }

  export type UserClientUncheckedCreateWithoutClientInput = {
    id?: number
    userId: number
  }

  export type UserClientCreateOrConnectWithoutClientInput = {
    where: UserClientWhereUniqueInput
    create: XOR<UserClientCreateWithoutClientInput, UserClientUncheckedCreateWithoutClientInput>
  }

  export type UserClientCreateManyClientInputEnvelope = {
    data: UserClientCreateManyClientInput | UserClientCreateManyClientInput[]
    skipDuplicates?: boolean
  }

  export type DetaliiUpsertWithoutClientInput = {
    update: XOR<DetaliiUpdateWithoutClientInput, DetaliiUncheckedUpdateWithoutClientInput>
    create: XOR<DetaliiCreateWithoutClientInput, DetaliiUncheckedCreateWithoutClientInput>
    where?: DetaliiWhereInput
  }

  export type DetaliiUpdateToOneWithWhereWithoutClientInput = {
    where?: DetaliiWhereInput
    data: XOR<DetaliiUpdateWithoutClientInput, DetaliiUncheckedUpdateWithoutClientInput>
  }

  export type DetaliiUpdateWithoutClientInput = {
    registruUC?: BoolFieldUpdateOperationsInput | boolean
    registruEvFiscala?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
    ofSpalareBani?: BoolFieldUpdateOperationsInput | boolean
    regulamentOrdineInterioara?: BoolFieldUpdateOperationsInput | boolean
    manualPoliticiContabile?: BoolFieldUpdateOperationsInput | boolean
    adresaRevisal?: BoolFieldUpdateOperationsInput | boolean
    parolaITM?: NullableStringFieldUpdateOperationsInput | string | null
    depunereDeclaratiiOnline?: BoolFieldUpdateOperationsInput | boolean
    accesDosarFiscal?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
  }

  export type DetaliiUncheckedUpdateWithoutClientInput = {
    id?: IntFieldUpdateOperationsInput | number
    registruUC?: BoolFieldUpdateOperationsInput | boolean
    registruEvFiscala?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
    ofSpalareBani?: BoolFieldUpdateOperationsInput | boolean
    regulamentOrdineInterioara?: BoolFieldUpdateOperationsInput | boolean
    manualPoliticiContabile?: BoolFieldUpdateOperationsInput | boolean
    adresaRevisal?: BoolFieldUpdateOperationsInput | boolean
    parolaITM?: NullableStringFieldUpdateOperationsInput | string | null
    depunereDeclaratiiOnline?: BoolFieldUpdateOperationsInput | boolean
    accesDosarFiscal?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
  }

  export type PunctDeLucruUpsertWithWhereUniqueWithoutClientInput = {
    where: PunctDeLucruWhereUniqueInput
    update: XOR<PunctDeLucruUpdateWithoutClientInput, PunctDeLucruUncheckedUpdateWithoutClientInput>
    create: XOR<PunctDeLucruCreateWithoutClientInput, PunctDeLucruUncheckedCreateWithoutClientInput>
  }

  export type PunctDeLucruUpdateWithWhereUniqueWithoutClientInput = {
    where: PunctDeLucruWhereUniqueInput
    data: XOR<PunctDeLucruUpdateWithoutClientInput, PunctDeLucruUncheckedUpdateWithoutClientInput>
  }

  export type PunctDeLucruUpdateManyWithWhereWithoutClientInput = {
    where: PunctDeLucruScalarWhereInput
    data: XOR<PunctDeLucruUpdateManyMutationInput, PunctDeLucruUncheckedUpdateManyWithoutClientInput>
  }

  export type PunctDeLucruScalarWhereInput = {
    AND?: PunctDeLucruScalarWhereInput | PunctDeLucruScalarWhereInput[]
    OR?: PunctDeLucruScalarWhereInput[]
    NOT?: PunctDeLucruScalarWhereInput | PunctDeLucruScalarWhereInput[]
    id?: IntFilter<"PunctDeLucru"> | number
    denumire?: StringFilter<"PunctDeLucru"> | string
    deLa?: DateTimeFilter<"PunctDeLucru"> | Date | string
    panaLa?: DateTimeNullableFilter<"PunctDeLucru"> | Date | string | null
    administratie?: EnumAdministratieFilter<"PunctDeLucru"> | $Enums.Administratie
    registruUC?: BoolFilter<"PunctDeLucru"> | boolean
    salariati?: IntFilter<"PunctDeLucru"> | number
    cui?: StringNullableFilter<"PunctDeLucru"> | string | null
    casaDeMarcat?: BoolFilter<"PunctDeLucru"> | boolean
    clientId?: IntFilter<"PunctDeLucru"> | number
  }

  export type IstoricUpsertWithWhereUniqueWithoutClientInput = {
    where: IstoricWhereUniqueInput
    update: XOR<IstoricUpdateWithoutClientInput, IstoricUncheckedUpdateWithoutClientInput>
    create: XOR<IstoricCreateWithoutClientInput, IstoricUncheckedCreateWithoutClientInput>
  }

  export type IstoricUpdateWithWhereUniqueWithoutClientInput = {
    where: IstoricWhereUniqueInput
    data: XOR<IstoricUpdateWithoutClientInput, IstoricUncheckedUpdateWithoutClientInput>
  }

  export type IstoricUpdateManyWithWhereWithoutClientInput = {
    where: IstoricScalarWhereInput
    data: XOR<IstoricUpdateManyMutationInput, IstoricUncheckedUpdateManyWithoutClientInput>
  }

  export type IstoricScalarWhereInput = {
    AND?: IstoricScalarWhereInput | IstoricScalarWhereInput[]
    OR?: IstoricScalarWhereInput[]
    NOT?: IstoricScalarWhereInput | IstoricScalarWhereInput[]
    id?: IntFilter<"Istoric"> | number
    anul?: IntFilter<"Istoric"> | number
    cifraAfaceri?: FloatFilter<"Istoric"> | number
    inventar?: BoolFilter<"Istoric"> | boolean
    bilantSemIun?: EnumDaNuNuECazulFilter<"Istoric"> | $Enums.DaNuNuECazul
    bilantAnual?: EnumDaNuNuECazulFilter<"Istoric"> | $Enums.DaNuNuECazul
    clientId?: IntFilter<"Istoric"> | number
  }

  export type TaskUpsertWithWhereUniqueWithoutClientInput = {
    where: TaskWhereUniqueInput
    update: XOR<TaskUpdateWithoutClientInput, TaskUncheckedUpdateWithoutClientInput>
    create: XOR<TaskCreateWithoutClientInput, TaskUncheckedCreateWithoutClientInput>
  }

  export type TaskUpdateWithWhereUniqueWithoutClientInput = {
    where: TaskWhereUniqueInput
    data: XOR<TaskUpdateWithoutClientInput, TaskUncheckedUpdateWithoutClientInput>
  }

  export type TaskUpdateManyWithWhereWithoutClientInput = {
    where: TaskScalarWhereInput
    data: XOR<TaskUpdateManyMutationInput, TaskUncheckedUpdateManyWithoutClientInput>
  }

  export type UserClientUpsertWithWhereUniqueWithoutClientInput = {
    where: UserClientWhereUniqueInput
    update: XOR<UserClientUpdateWithoutClientInput, UserClientUncheckedUpdateWithoutClientInput>
    create: XOR<UserClientCreateWithoutClientInput, UserClientUncheckedCreateWithoutClientInput>
  }

  export type UserClientUpdateWithWhereUniqueWithoutClientInput = {
    where: UserClientWhereUniqueInput
    data: XOR<UserClientUpdateWithoutClientInput, UserClientUncheckedUpdateWithoutClientInput>
  }

  export type UserClientUpdateManyWithWhereWithoutClientInput = {
    where: UserClientScalarWhereInput
    data: XOR<UserClientUpdateManyMutationInput, UserClientUncheckedUpdateManyWithoutClientInput>
  }

  export type ClientCreateWithoutDetaliiInput = {
    denumire: string
    tip: $Enums.Tip
    cui: string
    activa: boolean
    dataVerificarii?: Date | string | null
    adresa?: string | null
    administratie: $Enums.Administratie
    impozit?: $Enums.Impozit | null
    platitorTVA: $Enums.DaLunarTrim
    tvaLaIncasare?: boolean | null
    areCodTVAUE?: boolean | null
    codTVAUE?: string | null
    operatiuneUE?: boolean | null
    dividende?: boolean | null
    salariati?: $Enums.DaLunarTrim | null
    casaDeMarcat?: boolean | null
    dataExpSediuSocial?: Date | string | null
    dataExpMandatAdmin?: Date | string | null
    dataCertificatFiscal?: Date | string | null
    dataFisaPlatitor?: Date | string | null
    dataVectFiscal?: Date | string | null
    createdAt?: Date | string
    updatedAt?: Date | string
    puncteDeLucru?: PunctDeLucruCreateNestedManyWithoutClientInput
    istorice?: IstoricCreateNestedManyWithoutClientInput
    tasks?: TaskCreateNestedManyWithoutClientInput
    users?: UserClientCreateNestedManyWithoutClientInput
  }

  export type ClientUncheckedCreateWithoutDetaliiInput = {
    id?: number
    denumire: string
    tip: $Enums.Tip
    cui: string
    activa: boolean
    dataVerificarii?: Date | string | null
    adresa?: string | null
    administratie: $Enums.Administratie
    impozit?: $Enums.Impozit | null
    platitorTVA: $Enums.DaLunarTrim
    tvaLaIncasare?: boolean | null
    areCodTVAUE?: boolean | null
    codTVAUE?: string | null
    operatiuneUE?: boolean | null
    dividende?: boolean | null
    salariati?: $Enums.DaLunarTrim | null
    casaDeMarcat?: boolean | null
    dataExpSediuSocial?: Date | string | null
    dataExpMandatAdmin?: Date | string | null
    dataCertificatFiscal?: Date | string | null
    dataFisaPlatitor?: Date | string | null
    dataVectFiscal?: Date | string | null
    createdAt?: Date | string
    updatedAt?: Date | string
    puncteDeLucru?: PunctDeLucruUncheckedCreateNestedManyWithoutClientInput
    istorice?: IstoricUncheckedCreateNestedManyWithoutClientInput
    tasks?: TaskUncheckedCreateNestedManyWithoutClientInput
    users?: UserClientUncheckedCreateNestedManyWithoutClientInput
  }

  export type ClientCreateOrConnectWithoutDetaliiInput = {
    where: ClientWhereUniqueInput
    create: XOR<ClientCreateWithoutDetaliiInput, ClientUncheckedCreateWithoutDetaliiInput>
  }

  export type ClientUpsertWithoutDetaliiInput = {
    update: XOR<ClientUpdateWithoutDetaliiInput, ClientUncheckedUpdateWithoutDetaliiInput>
    create: XOR<ClientCreateWithoutDetaliiInput, ClientUncheckedCreateWithoutDetaliiInput>
    where?: ClientWhereInput
  }

  export type ClientUpdateToOneWithWhereWithoutDetaliiInput = {
    where?: ClientWhereInput
    data: XOR<ClientUpdateWithoutDetaliiInput, ClientUncheckedUpdateWithoutDetaliiInput>
  }

  export type ClientUpdateWithoutDetaliiInput = {
    denumire?: StringFieldUpdateOperationsInput | string
    tip?: EnumTipFieldUpdateOperationsInput | $Enums.Tip
    cui?: StringFieldUpdateOperationsInput | string
    activa?: BoolFieldUpdateOperationsInput | boolean
    dataVerificarii?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    adresa?: NullableStringFieldUpdateOperationsInput | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    impozit?: NullableEnumImpozitFieldUpdateOperationsInput | $Enums.Impozit | null
    platitorTVA?: EnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim
    tvaLaIncasare?: NullableBoolFieldUpdateOperationsInput | boolean | null
    areCodTVAUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    codTVAUE?: NullableStringFieldUpdateOperationsInput | string | null
    operatiuneUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dividende?: NullableBoolFieldUpdateOperationsInput | boolean | null
    salariati?: NullableEnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim | null
    casaDeMarcat?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dataExpSediuSocial?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataExpMandatAdmin?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataCertificatFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataFisaPlatitor?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataVectFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    createdAt?: DateTimeFieldUpdateOperationsInput | Date | string
    updatedAt?: DateTimeFieldUpdateOperationsInput | Date | string
    puncteDeLucru?: PunctDeLucruUpdateManyWithoutClientNestedInput
    istorice?: IstoricUpdateManyWithoutClientNestedInput
    tasks?: TaskUpdateManyWithoutClientNestedInput
    users?: UserClientUpdateManyWithoutClientNestedInput
  }

  export type ClientUncheckedUpdateWithoutDetaliiInput = {
    id?: IntFieldUpdateOperationsInput | number
    denumire?: StringFieldUpdateOperationsInput | string
    tip?: EnumTipFieldUpdateOperationsInput | $Enums.Tip
    cui?: StringFieldUpdateOperationsInput | string
    activa?: BoolFieldUpdateOperationsInput | boolean
    dataVerificarii?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    adresa?: NullableStringFieldUpdateOperationsInput | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    impozit?: NullableEnumImpozitFieldUpdateOperationsInput | $Enums.Impozit | null
    platitorTVA?: EnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim
    tvaLaIncasare?: NullableBoolFieldUpdateOperationsInput | boolean | null
    areCodTVAUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    codTVAUE?: NullableStringFieldUpdateOperationsInput | string | null
    operatiuneUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dividende?: NullableBoolFieldUpdateOperationsInput | boolean | null
    salariati?: NullableEnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim | null
    casaDeMarcat?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dataExpSediuSocial?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataExpMandatAdmin?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataCertificatFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataFisaPlatitor?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataVectFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    createdAt?: DateTimeFieldUpdateOperationsInput | Date | string
    updatedAt?: DateTimeFieldUpdateOperationsInput | Date | string
    puncteDeLucru?: PunctDeLucruUncheckedUpdateManyWithoutClientNestedInput
    istorice?: IstoricUncheckedUpdateManyWithoutClientNestedInput
    tasks?: TaskUncheckedUpdateManyWithoutClientNestedInput
    users?: UserClientUncheckedUpdateManyWithoutClientNestedInput
  }

  export type ClientCreateWithoutPuncteDeLucruInput = {
    denumire: string
    tip: $Enums.Tip
    cui: string
    activa: boolean
    dataVerificarii?: Date | string | null
    adresa?: string | null
    administratie: $Enums.Administratie
    impozit?: $Enums.Impozit | null
    platitorTVA: $Enums.DaLunarTrim
    tvaLaIncasare?: boolean | null
    areCodTVAUE?: boolean | null
    codTVAUE?: string | null
    operatiuneUE?: boolean | null
    dividende?: boolean | null
    salariati?: $Enums.DaLunarTrim | null
    casaDeMarcat?: boolean | null
    dataExpSediuSocial?: Date | string | null
    dataExpMandatAdmin?: Date | string | null
    dataCertificatFiscal?: Date | string | null
    dataFisaPlatitor?: Date | string | null
    dataVectFiscal?: Date | string | null
    createdAt?: Date | string
    updatedAt?: Date | string
    detalii?: DetaliiCreateNestedOneWithoutClientInput
    istorice?: IstoricCreateNestedManyWithoutClientInput
    tasks?: TaskCreateNestedManyWithoutClientInput
    users?: UserClientCreateNestedManyWithoutClientInput
  }

  export type ClientUncheckedCreateWithoutPuncteDeLucruInput = {
    id?: number
    denumire: string
    tip: $Enums.Tip
    cui: string
    activa: boolean
    dataVerificarii?: Date | string | null
    adresa?: string | null
    administratie: $Enums.Administratie
    impozit?: $Enums.Impozit | null
    platitorTVA: $Enums.DaLunarTrim
    tvaLaIncasare?: boolean | null
    areCodTVAUE?: boolean | null
    codTVAUE?: string | null
    operatiuneUE?: boolean | null
    dividende?: boolean | null
    salariati?: $Enums.DaLunarTrim | null
    casaDeMarcat?: boolean | null
    dataExpSediuSocial?: Date | string | null
    dataExpMandatAdmin?: Date | string | null
    dataCertificatFiscal?: Date | string | null
    dataFisaPlatitor?: Date | string | null
    dataVectFiscal?: Date | string | null
    createdAt?: Date | string
    updatedAt?: Date | string
    detalii?: DetaliiUncheckedCreateNestedOneWithoutClientInput
    istorice?: IstoricUncheckedCreateNestedManyWithoutClientInput
    tasks?: TaskUncheckedCreateNestedManyWithoutClientInput
    users?: UserClientUncheckedCreateNestedManyWithoutClientInput
  }

  export type ClientCreateOrConnectWithoutPuncteDeLucruInput = {
    where: ClientWhereUniqueInput
    create: XOR<ClientCreateWithoutPuncteDeLucruInput, ClientUncheckedCreateWithoutPuncteDeLucruInput>
  }

  export type ClientUpsertWithoutPuncteDeLucruInput = {
    update: XOR<ClientUpdateWithoutPuncteDeLucruInput, ClientUncheckedUpdateWithoutPuncteDeLucruInput>
    create: XOR<ClientCreateWithoutPuncteDeLucruInput, ClientUncheckedCreateWithoutPuncteDeLucruInput>
    where?: ClientWhereInput
  }

  export type ClientUpdateToOneWithWhereWithoutPuncteDeLucruInput = {
    where?: ClientWhereInput
    data: XOR<ClientUpdateWithoutPuncteDeLucruInput, ClientUncheckedUpdateWithoutPuncteDeLucruInput>
  }

  export type ClientUpdateWithoutPuncteDeLucruInput = {
    denumire?: StringFieldUpdateOperationsInput | string
    tip?: EnumTipFieldUpdateOperationsInput | $Enums.Tip
    cui?: StringFieldUpdateOperationsInput | string
    activa?: BoolFieldUpdateOperationsInput | boolean
    dataVerificarii?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    adresa?: NullableStringFieldUpdateOperationsInput | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    impozit?: NullableEnumImpozitFieldUpdateOperationsInput | $Enums.Impozit | null
    platitorTVA?: EnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim
    tvaLaIncasare?: NullableBoolFieldUpdateOperationsInput | boolean | null
    areCodTVAUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    codTVAUE?: NullableStringFieldUpdateOperationsInput | string | null
    operatiuneUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dividende?: NullableBoolFieldUpdateOperationsInput | boolean | null
    salariati?: NullableEnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim | null
    casaDeMarcat?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dataExpSediuSocial?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataExpMandatAdmin?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataCertificatFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataFisaPlatitor?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataVectFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    createdAt?: DateTimeFieldUpdateOperationsInput | Date | string
    updatedAt?: DateTimeFieldUpdateOperationsInput | Date | string
    detalii?: DetaliiUpdateOneWithoutClientNestedInput
    istorice?: IstoricUpdateManyWithoutClientNestedInput
    tasks?: TaskUpdateManyWithoutClientNestedInput
    users?: UserClientUpdateManyWithoutClientNestedInput
  }

  export type ClientUncheckedUpdateWithoutPuncteDeLucruInput = {
    id?: IntFieldUpdateOperationsInput | number
    denumire?: StringFieldUpdateOperationsInput | string
    tip?: EnumTipFieldUpdateOperationsInput | $Enums.Tip
    cui?: StringFieldUpdateOperationsInput | string
    activa?: BoolFieldUpdateOperationsInput | boolean
    dataVerificarii?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    adresa?: NullableStringFieldUpdateOperationsInput | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    impozit?: NullableEnumImpozitFieldUpdateOperationsInput | $Enums.Impozit | null
    platitorTVA?: EnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim
    tvaLaIncasare?: NullableBoolFieldUpdateOperationsInput | boolean | null
    areCodTVAUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    codTVAUE?: NullableStringFieldUpdateOperationsInput | string | null
    operatiuneUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dividende?: NullableBoolFieldUpdateOperationsInput | boolean | null
    salariati?: NullableEnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim | null
    casaDeMarcat?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dataExpSediuSocial?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataExpMandatAdmin?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataCertificatFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataFisaPlatitor?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataVectFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    createdAt?: DateTimeFieldUpdateOperationsInput | Date | string
    updatedAt?: DateTimeFieldUpdateOperationsInput | Date | string
    detalii?: DetaliiUncheckedUpdateOneWithoutClientNestedInput
    istorice?: IstoricUncheckedUpdateManyWithoutClientNestedInput
    tasks?: TaskUncheckedUpdateManyWithoutClientNestedInput
    users?: UserClientUncheckedUpdateManyWithoutClientNestedInput
  }

  export type ClientCreateWithoutIstoriceInput = {
    denumire: string
    tip: $Enums.Tip
    cui: string
    activa: boolean
    dataVerificarii?: Date | string | null
    adresa?: string | null
    administratie: $Enums.Administratie
    impozit?: $Enums.Impozit | null
    platitorTVA: $Enums.DaLunarTrim
    tvaLaIncasare?: boolean | null
    areCodTVAUE?: boolean | null
    codTVAUE?: string | null
    operatiuneUE?: boolean | null
    dividende?: boolean | null
    salariati?: $Enums.DaLunarTrim | null
    casaDeMarcat?: boolean | null
    dataExpSediuSocial?: Date | string | null
    dataExpMandatAdmin?: Date | string | null
    dataCertificatFiscal?: Date | string | null
    dataFisaPlatitor?: Date | string | null
    dataVectFiscal?: Date | string | null
    createdAt?: Date | string
    updatedAt?: Date | string
    detalii?: DetaliiCreateNestedOneWithoutClientInput
    puncteDeLucru?: PunctDeLucruCreateNestedManyWithoutClientInput
    tasks?: TaskCreateNestedManyWithoutClientInput
    users?: UserClientCreateNestedManyWithoutClientInput
  }

  export type ClientUncheckedCreateWithoutIstoriceInput = {
    id?: number
    denumire: string
    tip: $Enums.Tip
    cui: string
    activa: boolean
    dataVerificarii?: Date | string | null
    adresa?: string | null
    administratie: $Enums.Administratie
    impozit?: $Enums.Impozit | null
    platitorTVA: $Enums.DaLunarTrim
    tvaLaIncasare?: boolean | null
    areCodTVAUE?: boolean | null
    codTVAUE?: string | null
    operatiuneUE?: boolean | null
    dividende?: boolean | null
    salariati?: $Enums.DaLunarTrim | null
    casaDeMarcat?: boolean | null
    dataExpSediuSocial?: Date | string | null
    dataExpMandatAdmin?: Date | string | null
    dataCertificatFiscal?: Date | string | null
    dataFisaPlatitor?: Date | string | null
    dataVectFiscal?: Date | string | null
    createdAt?: Date | string
    updatedAt?: Date | string
    detalii?: DetaliiUncheckedCreateNestedOneWithoutClientInput
    puncteDeLucru?: PunctDeLucruUncheckedCreateNestedManyWithoutClientInput
    tasks?: TaskUncheckedCreateNestedManyWithoutClientInput
    users?: UserClientUncheckedCreateNestedManyWithoutClientInput
  }

  export type ClientCreateOrConnectWithoutIstoriceInput = {
    where: ClientWhereUniqueInput
    create: XOR<ClientCreateWithoutIstoriceInput, ClientUncheckedCreateWithoutIstoriceInput>
  }

  export type ClientUpsertWithoutIstoriceInput = {
    update: XOR<ClientUpdateWithoutIstoriceInput, ClientUncheckedUpdateWithoutIstoriceInput>
    create: XOR<ClientCreateWithoutIstoriceInput, ClientUncheckedCreateWithoutIstoriceInput>
    where?: ClientWhereInput
  }

  export type ClientUpdateToOneWithWhereWithoutIstoriceInput = {
    where?: ClientWhereInput
    data: XOR<ClientUpdateWithoutIstoriceInput, ClientUncheckedUpdateWithoutIstoriceInput>
  }

  export type ClientUpdateWithoutIstoriceInput = {
    denumire?: StringFieldUpdateOperationsInput | string
    tip?: EnumTipFieldUpdateOperationsInput | $Enums.Tip
    cui?: StringFieldUpdateOperationsInput | string
    activa?: BoolFieldUpdateOperationsInput | boolean
    dataVerificarii?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    adresa?: NullableStringFieldUpdateOperationsInput | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    impozit?: NullableEnumImpozitFieldUpdateOperationsInput | $Enums.Impozit | null
    platitorTVA?: EnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim
    tvaLaIncasare?: NullableBoolFieldUpdateOperationsInput | boolean | null
    areCodTVAUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    codTVAUE?: NullableStringFieldUpdateOperationsInput | string | null
    operatiuneUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dividende?: NullableBoolFieldUpdateOperationsInput | boolean | null
    salariati?: NullableEnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim | null
    casaDeMarcat?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dataExpSediuSocial?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataExpMandatAdmin?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataCertificatFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataFisaPlatitor?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataVectFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    createdAt?: DateTimeFieldUpdateOperationsInput | Date | string
    updatedAt?: DateTimeFieldUpdateOperationsInput | Date | string
    detalii?: DetaliiUpdateOneWithoutClientNestedInput
    puncteDeLucru?: PunctDeLucruUpdateManyWithoutClientNestedInput
    tasks?: TaskUpdateManyWithoutClientNestedInput
    users?: UserClientUpdateManyWithoutClientNestedInput
  }

  export type ClientUncheckedUpdateWithoutIstoriceInput = {
    id?: IntFieldUpdateOperationsInput | number
    denumire?: StringFieldUpdateOperationsInput | string
    tip?: EnumTipFieldUpdateOperationsInput | $Enums.Tip
    cui?: StringFieldUpdateOperationsInput | string
    activa?: BoolFieldUpdateOperationsInput | boolean
    dataVerificarii?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    adresa?: NullableStringFieldUpdateOperationsInput | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    impozit?: NullableEnumImpozitFieldUpdateOperationsInput | $Enums.Impozit | null
    platitorTVA?: EnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim
    tvaLaIncasare?: NullableBoolFieldUpdateOperationsInput | boolean | null
    areCodTVAUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    codTVAUE?: NullableStringFieldUpdateOperationsInput | string | null
    operatiuneUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dividende?: NullableBoolFieldUpdateOperationsInput | boolean | null
    salariati?: NullableEnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim | null
    casaDeMarcat?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dataExpSediuSocial?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataExpMandatAdmin?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataCertificatFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataFisaPlatitor?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataVectFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    createdAt?: DateTimeFieldUpdateOperationsInput | Date | string
    updatedAt?: DateTimeFieldUpdateOperationsInput | Date | string
    detalii?: DetaliiUncheckedUpdateOneWithoutClientNestedInput
    puncteDeLucru?: PunctDeLucruUncheckedUpdateManyWithoutClientNestedInput
    tasks?: TaskUncheckedUpdateManyWithoutClientNestedInput
    users?: UserClientUncheckedUpdateManyWithoutClientNestedInput
  }

  export type UserCreateWithoutTasksInput = {
    email: string
    name?: string | null
    password?: string | null
    rol?: $Enums.Role
    clients?: UserClientCreateNestedManyWithoutUserInput
  }

  export type UserUncheckedCreateWithoutTasksInput = {
    id?: number
    email: string
    name?: string | null
    password?: string | null
    rol?: $Enums.Role
    clients?: UserClientUncheckedCreateNestedManyWithoutUserInput
  }

  export type UserCreateOrConnectWithoutTasksInput = {
    where: UserWhereUniqueInput
    create: XOR<UserCreateWithoutTasksInput, UserUncheckedCreateWithoutTasksInput>
  }

  export type ClientCreateWithoutTasksInput = {
    denumire: string
    tip: $Enums.Tip
    cui: string
    activa: boolean
    dataVerificarii?: Date | string | null
    adresa?: string | null
    administratie: $Enums.Administratie
    impozit?: $Enums.Impozit | null
    platitorTVA: $Enums.DaLunarTrim
    tvaLaIncasare?: boolean | null
    areCodTVAUE?: boolean | null
    codTVAUE?: string | null
    operatiuneUE?: boolean | null
    dividende?: boolean | null
    salariati?: $Enums.DaLunarTrim | null
    casaDeMarcat?: boolean | null
    dataExpSediuSocial?: Date | string | null
    dataExpMandatAdmin?: Date | string | null
    dataCertificatFiscal?: Date | string | null
    dataFisaPlatitor?: Date | string | null
    dataVectFiscal?: Date | string | null
    createdAt?: Date | string
    updatedAt?: Date | string
    detalii?: DetaliiCreateNestedOneWithoutClientInput
    puncteDeLucru?: PunctDeLucruCreateNestedManyWithoutClientInput
    istorice?: IstoricCreateNestedManyWithoutClientInput
    users?: UserClientCreateNestedManyWithoutClientInput
  }

  export type ClientUncheckedCreateWithoutTasksInput = {
    id?: number
    denumire: string
    tip: $Enums.Tip
    cui: string
    activa: boolean
    dataVerificarii?: Date | string | null
    adresa?: string | null
    administratie: $Enums.Administratie
    impozit?: $Enums.Impozit | null
    platitorTVA: $Enums.DaLunarTrim
    tvaLaIncasare?: boolean | null
    areCodTVAUE?: boolean | null
    codTVAUE?: string | null
    operatiuneUE?: boolean | null
    dividende?: boolean | null
    salariati?: $Enums.DaLunarTrim | null
    casaDeMarcat?: boolean | null
    dataExpSediuSocial?: Date | string | null
    dataExpMandatAdmin?: Date | string | null
    dataCertificatFiscal?: Date | string | null
    dataFisaPlatitor?: Date | string | null
    dataVectFiscal?: Date | string | null
    createdAt?: Date | string
    updatedAt?: Date | string
    detalii?: DetaliiUncheckedCreateNestedOneWithoutClientInput
    puncteDeLucru?: PunctDeLucruUncheckedCreateNestedManyWithoutClientInput
    istorice?: IstoricUncheckedCreateNestedManyWithoutClientInput
    users?: UserClientUncheckedCreateNestedManyWithoutClientInput
  }

  export type ClientCreateOrConnectWithoutTasksInput = {
    where: ClientWhereUniqueInput
    create: XOR<ClientCreateWithoutTasksInput, ClientUncheckedCreateWithoutTasksInput>
  }

  export type UserUpsertWithoutTasksInput = {
    update: XOR<UserUpdateWithoutTasksInput, UserUncheckedUpdateWithoutTasksInput>
    create: XOR<UserCreateWithoutTasksInput, UserUncheckedCreateWithoutTasksInput>
    where?: UserWhereInput
  }

  export type UserUpdateToOneWithWhereWithoutTasksInput = {
    where?: UserWhereInput
    data: XOR<UserUpdateWithoutTasksInput, UserUncheckedUpdateWithoutTasksInput>
  }

  export type UserUpdateWithoutTasksInput = {
    email?: StringFieldUpdateOperationsInput | string
    name?: NullableStringFieldUpdateOperationsInput | string | null
    password?: NullableStringFieldUpdateOperationsInput | string | null
    rol?: EnumRoleFieldUpdateOperationsInput | $Enums.Role
    clients?: UserClientUpdateManyWithoutUserNestedInput
  }

  export type UserUncheckedUpdateWithoutTasksInput = {
    id?: IntFieldUpdateOperationsInput | number
    email?: StringFieldUpdateOperationsInput | string
    name?: NullableStringFieldUpdateOperationsInput | string | null
    password?: NullableStringFieldUpdateOperationsInput | string | null
    rol?: EnumRoleFieldUpdateOperationsInput | $Enums.Role
    clients?: UserClientUncheckedUpdateManyWithoutUserNestedInput
  }

  export type ClientUpsertWithoutTasksInput = {
    update: XOR<ClientUpdateWithoutTasksInput, ClientUncheckedUpdateWithoutTasksInput>
    create: XOR<ClientCreateWithoutTasksInput, ClientUncheckedCreateWithoutTasksInput>
    where?: ClientWhereInput
  }

  export type ClientUpdateToOneWithWhereWithoutTasksInput = {
    where?: ClientWhereInput
    data: XOR<ClientUpdateWithoutTasksInput, ClientUncheckedUpdateWithoutTasksInput>
  }

  export type ClientUpdateWithoutTasksInput = {
    denumire?: StringFieldUpdateOperationsInput | string
    tip?: EnumTipFieldUpdateOperationsInput | $Enums.Tip
    cui?: StringFieldUpdateOperationsInput | string
    activa?: BoolFieldUpdateOperationsInput | boolean
    dataVerificarii?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    adresa?: NullableStringFieldUpdateOperationsInput | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    impozit?: NullableEnumImpozitFieldUpdateOperationsInput | $Enums.Impozit | null
    platitorTVA?: EnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim
    tvaLaIncasare?: NullableBoolFieldUpdateOperationsInput | boolean | null
    areCodTVAUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    codTVAUE?: NullableStringFieldUpdateOperationsInput | string | null
    operatiuneUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dividende?: NullableBoolFieldUpdateOperationsInput | boolean | null
    salariati?: NullableEnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim | null
    casaDeMarcat?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dataExpSediuSocial?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataExpMandatAdmin?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataCertificatFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataFisaPlatitor?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataVectFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    createdAt?: DateTimeFieldUpdateOperationsInput | Date | string
    updatedAt?: DateTimeFieldUpdateOperationsInput | Date | string
    detalii?: DetaliiUpdateOneWithoutClientNestedInput
    puncteDeLucru?: PunctDeLucruUpdateManyWithoutClientNestedInput
    istorice?: IstoricUpdateManyWithoutClientNestedInput
    users?: UserClientUpdateManyWithoutClientNestedInput
  }

  export type ClientUncheckedUpdateWithoutTasksInput = {
    id?: IntFieldUpdateOperationsInput | number
    denumire?: StringFieldUpdateOperationsInput | string
    tip?: EnumTipFieldUpdateOperationsInput | $Enums.Tip
    cui?: StringFieldUpdateOperationsInput | string
    activa?: BoolFieldUpdateOperationsInput | boolean
    dataVerificarii?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    adresa?: NullableStringFieldUpdateOperationsInput | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    impozit?: NullableEnumImpozitFieldUpdateOperationsInput | $Enums.Impozit | null
    platitorTVA?: EnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim
    tvaLaIncasare?: NullableBoolFieldUpdateOperationsInput | boolean | null
    areCodTVAUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    codTVAUE?: NullableStringFieldUpdateOperationsInput | string | null
    operatiuneUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dividende?: NullableBoolFieldUpdateOperationsInput | boolean | null
    salariati?: NullableEnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim | null
    casaDeMarcat?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dataExpSediuSocial?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataExpMandatAdmin?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataCertificatFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataFisaPlatitor?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataVectFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    createdAt?: DateTimeFieldUpdateOperationsInput | Date | string
    updatedAt?: DateTimeFieldUpdateOperationsInput | Date | string
    detalii?: DetaliiUncheckedUpdateOneWithoutClientNestedInput
    puncteDeLucru?: PunctDeLucruUncheckedUpdateManyWithoutClientNestedInput
    istorice?: IstoricUncheckedUpdateManyWithoutClientNestedInput
    users?: UserClientUncheckedUpdateManyWithoutClientNestedInput
  }

  export type RuleConditionCreateWithoutRuleInput = {
    field: string
    operator?: $Enums.ConditionOperator
    value: string
  }

  export type RuleConditionUncheckedCreateWithoutRuleInput = {
    id?: number
    field: string
    operator?: $Enums.ConditionOperator
    value: string
  }

  export type RuleConditionCreateOrConnectWithoutRuleInput = {
    where: RuleConditionWhereUniqueInput
    create: XOR<RuleConditionCreateWithoutRuleInput, RuleConditionUncheckedCreateWithoutRuleInput>
  }

  export type RuleConditionCreateManyRuleInputEnvelope = {
    data: RuleConditionCreateManyRuleInput | RuleConditionCreateManyRuleInput[]
    skipDuplicates?: boolean
  }

  export type RuleConditionUpsertWithWhereUniqueWithoutRuleInput = {
    where: RuleConditionWhereUniqueInput
    update: XOR<RuleConditionUpdateWithoutRuleInput, RuleConditionUncheckedUpdateWithoutRuleInput>
    create: XOR<RuleConditionCreateWithoutRuleInput, RuleConditionUncheckedCreateWithoutRuleInput>
  }

  export type RuleConditionUpdateWithWhereUniqueWithoutRuleInput = {
    where: RuleConditionWhereUniqueInput
    data: XOR<RuleConditionUpdateWithoutRuleInput, RuleConditionUncheckedUpdateWithoutRuleInput>
  }

  export type RuleConditionUpdateManyWithWhereWithoutRuleInput = {
    where: RuleConditionScalarWhereInput
    data: XOR<RuleConditionUpdateManyMutationInput, RuleConditionUncheckedUpdateManyWithoutRuleInput>
  }

  export type RuleConditionScalarWhereInput = {
    AND?: RuleConditionScalarWhereInput | RuleConditionScalarWhereInput[]
    OR?: RuleConditionScalarWhereInput[]
    NOT?: RuleConditionScalarWhereInput | RuleConditionScalarWhereInput[]
    id?: IntFilter<"RuleCondition"> | number
    field?: StringFilter<"RuleCondition"> | string
    operator?: EnumConditionOperatorFilter<"RuleCondition"> | $Enums.ConditionOperator
    value?: StringFilter<"RuleCondition"> | string
    ruleId?: IntFilter<"RuleCondition"> | number
  }

  export type RuleCreateWithoutConditionsInput = {
    name: string
    description?: string | null
    frequency: $Enums.Frequency
    taskTitle: string
    taskNotes?: string | null
    active?: boolean
    createdAt?: Date | string
    updatedAt?: Date | string
  }

  export type RuleUncheckedCreateWithoutConditionsInput = {
    id?: number
    name: string
    description?: string | null
    frequency: $Enums.Frequency
    taskTitle: string
    taskNotes?: string | null
    active?: boolean
    createdAt?: Date | string
    updatedAt?: Date | string
  }

  export type RuleCreateOrConnectWithoutConditionsInput = {
    where: RuleWhereUniqueInput
    create: XOR<RuleCreateWithoutConditionsInput, RuleUncheckedCreateWithoutConditionsInput>
  }

  export type RuleUpsertWithoutConditionsInput = {
    update: XOR<RuleUpdateWithoutConditionsInput, RuleUncheckedUpdateWithoutConditionsInput>
    create: XOR<RuleCreateWithoutConditionsInput, RuleUncheckedCreateWithoutConditionsInput>
    where?: RuleWhereInput
  }

  export type RuleUpdateToOneWithWhereWithoutConditionsInput = {
    where?: RuleWhereInput
    data: XOR<RuleUpdateWithoutConditionsInput, RuleUncheckedUpdateWithoutConditionsInput>
  }

  export type RuleUpdateWithoutConditionsInput = {
    name?: StringFieldUpdateOperationsInput | string
    description?: NullableStringFieldUpdateOperationsInput | string | null
    frequency?: EnumFrequencyFieldUpdateOperationsInput | $Enums.Frequency
    taskTitle?: StringFieldUpdateOperationsInput | string
    taskNotes?: NullableStringFieldUpdateOperationsInput | string | null
    active?: BoolFieldUpdateOperationsInput | boolean
    createdAt?: DateTimeFieldUpdateOperationsInput | Date | string
    updatedAt?: DateTimeFieldUpdateOperationsInput | Date | string
  }

  export type RuleUncheckedUpdateWithoutConditionsInput = {
    id?: IntFieldUpdateOperationsInput | number
    name?: StringFieldUpdateOperationsInput | string
    description?: NullableStringFieldUpdateOperationsInput | string | null
    frequency?: EnumFrequencyFieldUpdateOperationsInput | $Enums.Frequency
    taskTitle?: StringFieldUpdateOperationsInput | string
    taskNotes?: NullableStringFieldUpdateOperationsInput | string | null
    active?: BoolFieldUpdateOperationsInput | boolean
    createdAt?: DateTimeFieldUpdateOperationsInput | Date | string
    updatedAt?: DateTimeFieldUpdateOperationsInput | Date | string
  }

  export type UserCreateWithoutClientsInput = {
    email: string
    name?: string | null
    password?: string | null
    rol?: $Enums.Role
    tasks?: TaskCreateNestedManyWithoutUserInput
  }

  export type UserUncheckedCreateWithoutClientsInput = {
    id?: number
    email: string
    name?: string | null
    password?: string | null
    rol?: $Enums.Role
    tasks?: TaskUncheckedCreateNestedManyWithoutUserInput
  }

  export type UserCreateOrConnectWithoutClientsInput = {
    where: UserWhereUniqueInput
    create: XOR<UserCreateWithoutClientsInput, UserUncheckedCreateWithoutClientsInput>
  }

  export type ClientCreateWithoutUsersInput = {
    denumire: string
    tip: $Enums.Tip
    cui: string
    activa: boolean
    dataVerificarii?: Date | string | null
    adresa?: string | null
    administratie: $Enums.Administratie
    impozit?: $Enums.Impozit | null
    platitorTVA: $Enums.DaLunarTrim
    tvaLaIncasare?: boolean | null
    areCodTVAUE?: boolean | null
    codTVAUE?: string | null
    operatiuneUE?: boolean | null
    dividende?: boolean | null
    salariati?: $Enums.DaLunarTrim | null
    casaDeMarcat?: boolean | null
    dataExpSediuSocial?: Date | string | null
    dataExpMandatAdmin?: Date | string | null
    dataCertificatFiscal?: Date | string | null
    dataFisaPlatitor?: Date | string | null
    dataVectFiscal?: Date | string | null
    createdAt?: Date | string
    updatedAt?: Date | string
    detalii?: DetaliiCreateNestedOneWithoutClientInput
    puncteDeLucru?: PunctDeLucruCreateNestedManyWithoutClientInput
    istorice?: IstoricCreateNestedManyWithoutClientInput
    tasks?: TaskCreateNestedManyWithoutClientInput
  }

  export type ClientUncheckedCreateWithoutUsersInput = {
    id?: number
    denumire: string
    tip: $Enums.Tip
    cui: string
    activa: boolean
    dataVerificarii?: Date | string | null
    adresa?: string | null
    administratie: $Enums.Administratie
    impozit?: $Enums.Impozit | null
    platitorTVA: $Enums.DaLunarTrim
    tvaLaIncasare?: boolean | null
    areCodTVAUE?: boolean | null
    codTVAUE?: string | null
    operatiuneUE?: boolean | null
    dividende?: boolean | null
    salariati?: $Enums.DaLunarTrim | null
    casaDeMarcat?: boolean | null
    dataExpSediuSocial?: Date | string | null
    dataExpMandatAdmin?: Date | string | null
    dataCertificatFiscal?: Date | string | null
    dataFisaPlatitor?: Date | string | null
    dataVectFiscal?: Date | string | null
    createdAt?: Date | string
    updatedAt?: Date | string
    detalii?: DetaliiUncheckedCreateNestedOneWithoutClientInput
    puncteDeLucru?: PunctDeLucruUncheckedCreateNestedManyWithoutClientInput
    istorice?: IstoricUncheckedCreateNestedManyWithoutClientInput
    tasks?: TaskUncheckedCreateNestedManyWithoutClientInput
  }

  export type ClientCreateOrConnectWithoutUsersInput = {
    where: ClientWhereUniqueInput
    create: XOR<ClientCreateWithoutUsersInput, ClientUncheckedCreateWithoutUsersInput>
  }

  export type UserUpsertWithoutClientsInput = {
    update: XOR<UserUpdateWithoutClientsInput, UserUncheckedUpdateWithoutClientsInput>
    create: XOR<UserCreateWithoutClientsInput, UserUncheckedCreateWithoutClientsInput>
    where?: UserWhereInput
  }

  export type UserUpdateToOneWithWhereWithoutClientsInput = {
    where?: UserWhereInput
    data: XOR<UserUpdateWithoutClientsInput, UserUncheckedUpdateWithoutClientsInput>
  }

  export type UserUpdateWithoutClientsInput = {
    email?: StringFieldUpdateOperationsInput | string
    name?: NullableStringFieldUpdateOperationsInput | string | null
    password?: NullableStringFieldUpdateOperationsInput | string | null
    rol?: EnumRoleFieldUpdateOperationsInput | $Enums.Role
    tasks?: TaskUpdateManyWithoutUserNestedInput
  }

  export type UserUncheckedUpdateWithoutClientsInput = {
    id?: IntFieldUpdateOperationsInput | number
    email?: StringFieldUpdateOperationsInput | string
    name?: NullableStringFieldUpdateOperationsInput | string | null
    password?: NullableStringFieldUpdateOperationsInput | string | null
    rol?: EnumRoleFieldUpdateOperationsInput | $Enums.Role
    tasks?: TaskUncheckedUpdateManyWithoutUserNestedInput
  }

  export type ClientUpsertWithoutUsersInput = {
    update: XOR<ClientUpdateWithoutUsersInput, ClientUncheckedUpdateWithoutUsersInput>
    create: XOR<ClientCreateWithoutUsersInput, ClientUncheckedCreateWithoutUsersInput>
    where?: ClientWhereInput
  }

  export type ClientUpdateToOneWithWhereWithoutUsersInput = {
    where?: ClientWhereInput
    data: XOR<ClientUpdateWithoutUsersInput, ClientUncheckedUpdateWithoutUsersInput>
  }

  export type ClientUpdateWithoutUsersInput = {
    denumire?: StringFieldUpdateOperationsInput | string
    tip?: EnumTipFieldUpdateOperationsInput | $Enums.Tip
    cui?: StringFieldUpdateOperationsInput | string
    activa?: BoolFieldUpdateOperationsInput | boolean
    dataVerificarii?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    adresa?: NullableStringFieldUpdateOperationsInput | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    impozit?: NullableEnumImpozitFieldUpdateOperationsInput | $Enums.Impozit | null
    platitorTVA?: EnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim
    tvaLaIncasare?: NullableBoolFieldUpdateOperationsInput | boolean | null
    areCodTVAUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    codTVAUE?: NullableStringFieldUpdateOperationsInput | string | null
    operatiuneUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dividende?: NullableBoolFieldUpdateOperationsInput | boolean | null
    salariati?: NullableEnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim | null
    casaDeMarcat?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dataExpSediuSocial?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataExpMandatAdmin?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataCertificatFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataFisaPlatitor?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataVectFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    createdAt?: DateTimeFieldUpdateOperationsInput | Date | string
    updatedAt?: DateTimeFieldUpdateOperationsInput | Date | string
    detalii?: DetaliiUpdateOneWithoutClientNestedInput
    puncteDeLucru?: PunctDeLucruUpdateManyWithoutClientNestedInput
    istorice?: IstoricUpdateManyWithoutClientNestedInput
    tasks?: TaskUpdateManyWithoutClientNestedInput
  }

  export type ClientUncheckedUpdateWithoutUsersInput = {
    id?: IntFieldUpdateOperationsInput | number
    denumire?: StringFieldUpdateOperationsInput | string
    tip?: EnumTipFieldUpdateOperationsInput | $Enums.Tip
    cui?: StringFieldUpdateOperationsInput | string
    activa?: BoolFieldUpdateOperationsInput | boolean
    dataVerificarii?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    adresa?: NullableStringFieldUpdateOperationsInput | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    impozit?: NullableEnumImpozitFieldUpdateOperationsInput | $Enums.Impozit | null
    platitorTVA?: EnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim
    tvaLaIncasare?: NullableBoolFieldUpdateOperationsInput | boolean | null
    areCodTVAUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    codTVAUE?: NullableStringFieldUpdateOperationsInput | string | null
    operatiuneUE?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dividende?: NullableBoolFieldUpdateOperationsInput | boolean | null
    salariati?: NullableEnumDaLunarTrimFieldUpdateOperationsInput | $Enums.DaLunarTrim | null
    casaDeMarcat?: NullableBoolFieldUpdateOperationsInput | boolean | null
    dataExpSediuSocial?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataExpMandatAdmin?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataCertificatFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataFisaPlatitor?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    dataVectFiscal?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    createdAt?: DateTimeFieldUpdateOperationsInput | Date | string
    updatedAt?: DateTimeFieldUpdateOperationsInput | Date | string
    detalii?: DetaliiUncheckedUpdateOneWithoutClientNestedInput
    puncteDeLucru?: PunctDeLucruUncheckedUpdateManyWithoutClientNestedInput
    istorice?: IstoricUncheckedUpdateManyWithoutClientNestedInput
    tasks?: TaskUncheckedUpdateManyWithoutClientNestedInput
  }

  export type TaskCreateManyUserInput = {
    id?: number
    done?: boolean
    title: string
    notes?: string | null
    blocked?: string | null
    objective?: string | null
    date: Date | string
    clientId: number
  }

  export type UserClientCreateManyUserInput = {
    id?: number
    clientId: number
  }

  export type TaskUpdateWithoutUserInput = {
    done?: BoolFieldUpdateOperationsInput | boolean
    title?: StringFieldUpdateOperationsInput | string
    notes?: NullableStringFieldUpdateOperationsInput | string | null
    blocked?: NullableStringFieldUpdateOperationsInput | string | null
    objective?: NullableStringFieldUpdateOperationsInput | string | null
    date?: DateTimeFieldUpdateOperationsInput | Date | string
    client?: ClientUpdateOneRequiredWithoutTasksNestedInput
  }

  export type TaskUncheckedUpdateWithoutUserInput = {
    id?: IntFieldUpdateOperationsInput | number
    done?: BoolFieldUpdateOperationsInput | boolean
    title?: StringFieldUpdateOperationsInput | string
    notes?: NullableStringFieldUpdateOperationsInput | string | null
    blocked?: NullableStringFieldUpdateOperationsInput | string | null
    objective?: NullableStringFieldUpdateOperationsInput | string | null
    date?: DateTimeFieldUpdateOperationsInput | Date | string
    clientId?: IntFieldUpdateOperationsInput | number
  }

  export type TaskUncheckedUpdateManyWithoutUserInput = {
    id?: IntFieldUpdateOperationsInput | number
    done?: BoolFieldUpdateOperationsInput | boolean
    title?: StringFieldUpdateOperationsInput | string
    notes?: NullableStringFieldUpdateOperationsInput | string | null
    blocked?: NullableStringFieldUpdateOperationsInput | string | null
    objective?: NullableStringFieldUpdateOperationsInput | string | null
    date?: DateTimeFieldUpdateOperationsInput | Date | string
    clientId?: IntFieldUpdateOperationsInput | number
  }

  export type UserClientUpdateWithoutUserInput = {
    client?: ClientUpdateOneRequiredWithoutUsersNestedInput
  }

  export type UserClientUncheckedUpdateWithoutUserInput = {
    id?: IntFieldUpdateOperationsInput | number
    clientId?: IntFieldUpdateOperationsInput | number
  }

  export type UserClientUncheckedUpdateManyWithoutUserInput = {
    id?: IntFieldUpdateOperationsInput | number
    clientId?: IntFieldUpdateOperationsInput | number
  }

  export type PunctDeLucruCreateManyClientInput = {
    id?: number
    denumire: string
    deLa: Date | string
    panaLa?: Date | string | null
    administratie: $Enums.Administratie
    registruUC: boolean
    salariati: number
    cui?: string | null
    casaDeMarcat: boolean
  }

  export type IstoricCreateManyClientInput = {
    id?: number
    anul: number
    cifraAfaceri: number
    inventar: boolean
    bilantSemIun: $Enums.DaNuNuECazul
    bilantAnual: $Enums.DaNuNuECazul
  }

  export type TaskCreateManyClientInput = {
    id?: number
    done?: boolean
    title: string
    notes?: string | null
    blocked?: string | null
    objective?: string | null
    date: Date | string
    userId: number
  }

  export type UserClientCreateManyClientInput = {
    id?: number
    userId: number
  }

  export type PunctDeLucruUpdateWithoutClientInput = {
    denumire?: StringFieldUpdateOperationsInput | string
    deLa?: DateTimeFieldUpdateOperationsInput | Date | string
    panaLa?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    registruUC?: BoolFieldUpdateOperationsInput | boolean
    salariati?: IntFieldUpdateOperationsInput | number
    cui?: NullableStringFieldUpdateOperationsInput | string | null
    casaDeMarcat?: BoolFieldUpdateOperationsInput | boolean
  }

  export type PunctDeLucruUncheckedUpdateWithoutClientInput = {
    id?: IntFieldUpdateOperationsInput | number
    denumire?: StringFieldUpdateOperationsInput | string
    deLa?: DateTimeFieldUpdateOperationsInput | Date | string
    panaLa?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    registruUC?: BoolFieldUpdateOperationsInput | boolean
    salariati?: IntFieldUpdateOperationsInput | number
    cui?: NullableStringFieldUpdateOperationsInput | string | null
    casaDeMarcat?: BoolFieldUpdateOperationsInput | boolean
  }

  export type PunctDeLucruUncheckedUpdateManyWithoutClientInput = {
    id?: IntFieldUpdateOperationsInput | number
    denumire?: StringFieldUpdateOperationsInput | string
    deLa?: DateTimeFieldUpdateOperationsInput | Date | string
    panaLa?: NullableDateTimeFieldUpdateOperationsInput | Date | string | null
    administratie?: EnumAdministratieFieldUpdateOperationsInput | $Enums.Administratie
    registruUC?: BoolFieldUpdateOperationsInput | boolean
    salariati?: IntFieldUpdateOperationsInput | number
    cui?: NullableStringFieldUpdateOperationsInput | string | null
    casaDeMarcat?: BoolFieldUpdateOperationsInput | boolean
  }

  export type IstoricUpdateWithoutClientInput = {
    anul?: IntFieldUpdateOperationsInput | number
    cifraAfaceri?: FloatFieldUpdateOperationsInput | number
    inventar?: BoolFieldUpdateOperationsInput | boolean
    bilantSemIun?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
    bilantAnual?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
  }

  export type IstoricUncheckedUpdateWithoutClientInput = {
    id?: IntFieldUpdateOperationsInput | number
    anul?: IntFieldUpdateOperationsInput | number
    cifraAfaceri?: FloatFieldUpdateOperationsInput | number
    inventar?: BoolFieldUpdateOperationsInput | boolean
    bilantSemIun?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
    bilantAnual?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
  }

  export type IstoricUncheckedUpdateManyWithoutClientInput = {
    id?: IntFieldUpdateOperationsInput | number
    anul?: IntFieldUpdateOperationsInput | number
    cifraAfaceri?: FloatFieldUpdateOperationsInput | number
    inventar?: BoolFieldUpdateOperationsInput | boolean
    bilantSemIun?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
    bilantAnual?: EnumDaNuNuECazulFieldUpdateOperationsInput | $Enums.DaNuNuECazul
  }

  export type TaskUpdateWithoutClientInput = {
    done?: BoolFieldUpdateOperationsInput | boolean
    title?: StringFieldUpdateOperationsInput | string
    notes?: NullableStringFieldUpdateOperationsInput | string | null
    blocked?: NullableStringFieldUpdateOperationsInput | string | null
    objective?: NullableStringFieldUpdateOperationsInput | string | null
    date?: DateTimeFieldUpdateOperationsInput | Date | string
    user?: UserUpdateOneRequiredWithoutTasksNestedInput
  }

  export type TaskUncheckedUpdateWithoutClientInput = {
    id?: IntFieldUpdateOperationsInput | number
    done?: BoolFieldUpdateOperationsInput | boolean
    title?: StringFieldUpdateOperationsInput | string
    notes?: NullableStringFieldUpdateOperationsInput | string | null
    blocked?: NullableStringFieldUpdateOperationsInput | string | null
    objective?: NullableStringFieldUpdateOperationsInput | string | null
    date?: DateTimeFieldUpdateOperationsInput | Date | string
    userId?: IntFieldUpdateOperationsInput | number
  }

  export type TaskUncheckedUpdateManyWithoutClientInput = {
    id?: IntFieldUpdateOperationsInput | number
    done?: BoolFieldUpdateOperationsInput | boolean
    title?: StringFieldUpdateOperationsInput | string
    notes?: NullableStringFieldUpdateOperationsInput | string | null
    blocked?: NullableStringFieldUpdateOperationsInput | string | null
    objective?: NullableStringFieldUpdateOperationsInput | string | null
    date?: DateTimeFieldUpdateOperationsInput | Date | string
    userId?: IntFieldUpdateOperationsInput | number
  }

  export type UserClientUpdateWithoutClientInput = {
    user?: UserUpdateOneRequiredWithoutClientsNestedInput
  }

  export type UserClientUncheckedUpdateWithoutClientInput = {
    id?: IntFieldUpdateOperationsInput | number
    userId?: IntFieldUpdateOperationsInput | number
  }

  export type UserClientUncheckedUpdateManyWithoutClientInput = {
    id?: IntFieldUpdateOperationsInput | number
    userId?: IntFieldUpdateOperationsInput | number
  }

  export type RuleConditionCreateManyRuleInput = {
    id?: number
    field: string
    operator?: $Enums.ConditionOperator
    value: string
  }

  export type RuleConditionUpdateWithoutRuleInput = {
    field?: StringFieldUpdateOperationsInput | string
    operator?: EnumConditionOperatorFieldUpdateOperationsInput | $Enums.ConditionOperator
    value?: StringFieldUpdateOperationsInput | string
  }

  export type RuleConditionUncheckedUpdateWithoutRuleInput = {
    id?: IntFieldUpdateOperationsInput | number
    field?: StringFieldUpdateOperationsInput | string
    operator?: EnumConditionOperatorFieldUpdateOperationsInput | $Enums.ConditionOperator
    value?: StringFieldUpdateOperationsInput | string
  }

  export type RuleConditionUncheckedUpdateManyWithoutRuleInput = {
    id?: IntFieldUpdateOperationsInput | number
    field?: StringFieldUpdateOperationsInput | string
    operator?: EnumConditionOperatorFieldUpdateOperationsInput | $Enums.ConditionOperator
    value?: StringFieldUpdateOperationsInput | string
  }



  /**
   * Batch Payload for updateMany & deleteMany & createMany
   */

  export type BatchPayload = {
    count: number
  }

  /**
   * DMMF
   */
  export const dmmf: runtime.BaseDMMF
}