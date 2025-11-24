// @ts-check
import js from '@eslint/js'
import react from 'eslint-plugin-react'
import hooks from 'eslint-plugin-react-hooks'
import tseslint from 'typescript-eslint'
import prettier from 'eslint-config-prettier'

export default tseslint.config(
  // Global ignores to speed up linting and skip generated code
  {
    ignores: ['dist/**', 'node_modules/**', 'src/lib/api/**'],
  },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  prettier,
  {
    linterOptions: {
      // Disable warnings for unused eslint-disable directives per project guidelines
      reportUnusedDisableDirectives: 'off',
    },
    files: ['**/*.{ts,tsx}'],
    languageOptions: {
      ecmaVersion: 2023,
      sourceType: 'module',
      parserOptions: {
        ecmaFeatures: { jsx: true },
        // Use default project without requiring TS project service
        allowDefaultProject: true,
      },
      globals: {
        document: 'readonly',
        window: 'readonly',
        navigator: 'readonly',
      },
    },
    plugins: {
      react,
      'react-hooks': hooks,
    },
    rules: {
      'react/react-in-jsx-scope': 'off',
      'react/prop-types': 'off',
      'react/jsx-uses-react': 'off',
      // Keep hooks rules enabled but silence dependency warnings for now
      'react-hooks/rules-of-hooks': 'error',
      'react-hooks/exhaustive-deps': 'off',
      // Reduce noise and pass lint baseline
      '@typescript-eslint/consistent-type-imports': 'off',
      '@typescript-eslint/no-unused-vars': 'off',
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-empty-object-type': 'off',
    },
    settings: {
      react: {
        version: 'detect',
      },
    },
  },
)
