# SmartCleaner Testing Strategy

## Overview
Comprehensive test coverage for SmartCleaner app covering unit tests, integration tests, and UI tests.

## Test Structure

### 1. Unit Tests (`test/`)
Location: `app/src/test/java/com/example/smartcleaner/`

#### Data Layer Tests
- **HashUtilTest.kt** ✅
  - MD5 hash calculation
  - SHA-256 hash calculation
  - Perceptual hash (pHash) for images
  - Hamming distance calculation
  - Similarity percentage
  - Edge cases: empty files, large files

- **JunkClassifierTest.kt** ✅
  - Feature extraction (20 features)
  - Category detection
  - Confidence scoring
  - Rule-based fallback
  - Extension categorization
  - Hidden file detection

#### Domain Layer Tests
- **ScanLeftoverFilesUseCaseTest.kt** ✅
  - Repository integration
  - Flow emission
  - Filtering logic
  - Exception handling

- **ScanJunkUseCaseTest.kt** ✅
  - Multiple junk types
  - Size calculations
  - Empty results handling
  - Performance with large datasets

#### Presentation Layer Tests
- **LeftoverViewModelTest.kt** ✅
  - State management
  - File selection logic
  - Statistics calculation
  - Grouping by package
  - Delete operations

### 2. Integration Tests (`androidTest/`)
Location: `app/src/androidTest/java/com/example/smartcleaner/`

#### Worker Tests
- **AutoCleanWorkerTest.kt** ✅
  - WorkManager execution
  - Battery constraints
  - Notification delivery
  - Retry logic
  - Permission handling

#### UI Tests
- **LeftoverScreenTest.kt** ✅
  - Idle state display
  - File list rendering
  - Selection interactions
  - Delete confirmation
  - Group expansion

## Running Tests

### Unit Tests
```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests HashUtilTest

# With coverage
./gradlew testDebugUnitTest jacocoTestReport
```

### Integration Tests
```bash
# Run all instrumentation tests
./gradlew connectedAndroidTest

# Run specific test
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.smartcleaner.data.worker.AutoCleanWorkerTest
```

### UI Tests
```bash
# Run Compose UI tests
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.example.smartcleaner.presentation
```

## Test Dependencies

```kotlin
// Unit Testing
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito:mockito-core:5.3.1")
testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
testImplementation("androidx.arch.core:core-testing:2.2.0")

// Integration Testing
androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
androidTestImplementation("androidx.work:work-testing:2.9.0")

// Compose UI Testing
androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")
```

## Coverage Goals

### Current Coverage (Estimated)
- **Data Layer**: 75% (Hash utilities, ML classifier)
- **Domain Layer**: 80% (Use cases, models)
- **Presentation Layer**: 65% (ViewModels, some screens)
- **Overall**: 70%

### Target Coverage
- **Data Layer**: 85%
- **Domain Layer**: 90%
- **Presentation Layer**: 75%
- **Overall**: 80%

## Test Patterns

### Mocking with Mockito
```kotlin
@Mock
private lateinit var repository: LeftoverRepository

@Before
fun setup() {
    MockitoAnnotations.openMocks(this)
    `when`(repository.scanLeftoverFiles()).thenReturn(flowOf())
}
```

### Coroutine Testing
```kotlin
@ExperimentalCoroutinesApi
class ViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }
    
    @Test
    fun test() = runTest {
        // test code
        testDispatcher.scheduler.advanceUntilIdle()
    }
}
```

### Compose UI Testing
```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun test() {
    composeTestRule.setContent {
        MyScreen()
    }
    
    composeTestRule
        .onNodeWithText("Button")
        .performClick()
        .assertIsDisplayed()
}
```

## Continuous Integration

### GitHub Actions Configuration
```yaml
name: Android CI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run unit tests
        run: ./gradlew test
      - name: Generate coverage report
        run: ./gradlew jacocoTestReport
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
```

## Additional Test Files Needed

### Still to Implement:
1. **DuplicateFinderRepositoryImplTest.kt** - Test MD5 and pHash algorithms
2. **MessagingCleanerRepositoryImplTest.kt** - Test multi-app scanning
3. **StorageAnalyzerRepositoryImplTest.kt** - Test TreeMap generation
4. **RootRepositoryImplTest.kt** - Test LibSu integration (requires rooted test device)
5. **ClassifierViewModelTest.kt** - Test ML classifier UI logic
6. **DuplicateViewModelTest.kt** - Test duplicate finder UI
7. **NavigationTest.kt** - Test app navigation flows
8. **PermissionTest.kt** - Test permission request flows

## Performance Testing

### Benchmark Tests
```kotlin
@RunWith(AndroidJUnit4::class)
class ScanPerformanceTest {
    @Test
    fun benchmarkLeftoverScan() {
        val startTime = System.currentTimeMillis()
        // Perform scan on 1000 files
        val duration = System.currentTimeMillis() - startTime
        assertTrue(duration < 5000) // Should complete in < 5 seconds
    }
}
```

## Memory Leak Detection

### LeakCanary Integration
```kotlin
dependencies {
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
}
```

## Test Maintenance

### Best Practices:
1. ✅ Keep tests isolated and independent
2. ✅ Use descriptive test names
3. ✅ Follow AAA pattern (Arrange, Act, Assert)
4. ✅ Mock external dependencies
5. ✅ Test edge cases and error paths
6. ✅ Maintain test data fixtures
7. ✅ Run tests before committing
8. ✅ Review test coverage reports

---

**Test Status**: 🟢 Unit tests implemented for core modules
**Next Steps**: Complete integration tests and UI tests for remaining screens
